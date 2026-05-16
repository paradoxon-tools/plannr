import AsyncStorage from '@react-native-async-storage/async-storage';

import { getApiBaseUrl } from './settings';

export const CACHE_REFRESH_INTERVAL_MS = 5 * 60 * 1000;

type CacheRecord<T> = {
  data: T;
  savedAt: number;
};

export type CachedData<T> = {
  data: T;
  savedAt: number;
  isStale: boolean;
};

function cacheKey(baseUrl: string, key: string): string {
  return `plannr.cache.${baseUrl}.${key}`;
}

export async function getCachedData<T>(key: string, maxAgeMs = CACHE_REFRESH_INTERVAL_MS): Promise<CachedData<T> | null> {
  const baseUrl = await getApiBaseUrl();
  const value = await AsyncStorage.getItem(cacheKey(baseUrl, key));

  if (!value) {
    return null;
  }

  const record = JSON.parse(value) as CacheRecord<T>;
  return {
    data: record.data,
    savedAt: record.savedAt,
    isStale: Date.now() - record.savedAt >= maxAgeMs,
  };
}

export async function replaceCachedData<T>(key: string, data: T): Promise<number> {
  const baseUrl = await getApiBaseUrl();
  const savedAt = Date.now();
  const record: CacheRecord<T> = { data, savedAt };

  await AsyncStorage.setItem(cacheKey(baseUrl, key), JSON.stringify(record));
  return savedAt;
}
