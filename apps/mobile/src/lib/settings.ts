import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const API_BASE_URL_KEY = 'plannr.apiBaseUrl';

export const defaultApiBaseUrl =
  Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';

export async function getApiBaseUrl(): Promise<string> {
  return (await AsyncStorage.getItem(API_BASE_URL_KEY)) ?? defaultApiBaseUrl;
}

export async function setApiBaseUrl(value: string): Promise<void> {
  await AsyncStorage.setItem(API_BASE_URL_KEY, value.replace(/\/+$/, ''));
}
