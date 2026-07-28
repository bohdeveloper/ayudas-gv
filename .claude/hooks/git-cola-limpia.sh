#!/usr/bin/env bash
# Hook Stop — vigila que no se queden ficheros rezagados en la cola de git.
#
# Motivo (2026-07-28): el snapshot del grafo se quedaba indefinidamente pendiente
# porque cada commit lo volvia a ensuciar. Se corrigio en .gitignore, pero la regla
# general queda vigilada aqui: nada se queda para siempre en `git status`.
#
# Solo avisa de los REZAGADOS (sin tocar desde hace mas de UMBRAL_MIN minutos).
# Lo que acabas de editar es trabajo en curso y no se reporta: evita ruido en cada turno.
#
# La decision de que hacer con cada fichero (commit / .gitignore / borrar) esta en
# CLAUDE.md, seccion "Cola de git limpia".

UMBRAL_MIN=45

cd "${CLAUDE_PROJECT_DIR:-.}" 2>/dev/null || exit 0
git rev-parse --git-dir >/dev/null 2>&1 || exit 0

pendientes=$(git status --porcelain 2>/dev/null) || exit 0
[ -z "$pendientes" ] && exit 0

# Rezagados: existen en disco y no se han tocado en UMBRAL_MIN minutos.
# Los borrados (no existen) se reportan siempre: una eliminacion sin commit es un olvido.
rezagados=$(printf '%s\n' "$pendientes" | while IFS= read -r linea; do
  estado="${linea:0:2}"
  ruta="${linea:3}"
  ruta="${ruta%\"}"; ruta="${ruta#\"}"
  case "$estado" in
    *D*) printf '%s (borrado)\n' "$ruta"; continue ;;
  esac
  [ -e "$ruta" ] || continue
  if [ -n "$(find "$ruta" -maxdepth 0 ! -newermt "-${UMBRAL_MIN} minutes" 2>/dev/null)" ]; then
    printf '%s\n' "$ruta"
  fi
done)

[ -z "$rezagados" ] && exit 0

n=$(printf '%s\n' "$rezagados" | grep -c .)
lista=$(printf '%s\n' "$rezagados" | head -8 | sed 's/^/  - /')
[ "$n" -gt 8 ] && lista="$lista"$'\n'"  - (y $((n - 8)) mas)"

mensaje="Cola de git: $n fichero(s) llevan mas de ${UMBRAL_MIN} min pendientes."$'\n'"$lista"$'\n'"Decide por cada uno: commit, .gitignore o borrar (CLAUDE.md > Cola de git limpia)."

# Escapado a JSON. En este equipo no hay jq, pero si python (3.14). El ultimo
# recurso aplana los saltos de linea, que es feo pero nunca produce JSON invalido.
if command -v jq >/dev/null 2>&1; then
  printf '%s' "$mensaje" | jq -Rsc '{systemMessage: .}'
elif command -v python >/dev/null 2>&1; then
  printf '%s' "$mensaje" | python -c 'import json,sys; print(json.dumps({"systemMessage": sys.stdin.read()}))'
else
  printf '{"systemMessage":"%s"}\n' "$(printf '%s' "$mensaje" | tr '\n' ' ' | sed 's/\\/\\\\/g; s/"/\\"/g')"
fi
exit 0
