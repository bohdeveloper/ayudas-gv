import axiosClient from './axiosClient'
import type { LoginRequest, LoginResponse } from '../types'

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const { data } = await axiosClient.post<LoginResponse>('/auth/login', credentials)
  return data
}
