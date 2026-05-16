import { Link } from 'expo-router';
import { ChevronRight, RefreshCw, Settings } from 'lucide-react-native';
import { useCallback, useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Screen } from '@/components/Screen';
import { SkeletonBox } from '@/components/Skeleton';
import { StateBlock } from '@/components/StateBlock';
import { Account, api } from '@/lib/api';
import { CACHE_REFRESH_INTERVAL_MS, getCachedData, replaceCachedData } from '@/lib/cache';
import { getApiBaseUrl } from '@/lib/settings';

const ACCOUNTS_CACHE_KEY = 'accounts';

export default function DashboardScreen() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [apiBaseUrl, setApiBaseUrl] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async (forceRefresh = false) => {
    const [baseUrl, cachedAccounts] = await Promise.all([
      getApiBaseUrl(),
      getCachedData<Account[]>(ACCOUNTS_CACHE_KEY),
    ]);
    setApiBaseUrl(baseUrl);

    if (cachedAccounts) {
      setAccounts(cachedAccounts.data);
    }

    if (!forceRefresh && cachedAccounts && !cachedAccounts.isStale) {
      setLoading(false);
      return;
    }

    setLoading(!cachedAccounts);
    try {
      setError(null);
      const accountData = await api.listAccounts();
      await replaceCachedData(ACCOUNTS_CACHE_KEY, accountData);
      setAccounts(accountData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load accounts');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
    const intervalId = setInterval(() => {
      void load();
    }, CACHE_REFRESH_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [load]);

  const showSkeleton = loading && accounts.length === 0 && !error;

  return (
    <Screen>
      <View style={styles.header}>
        <View>
          <Text style={styles.eyebrow}>Server</Text>
          {apiBaseUrl ? (
            <Text style={styles.serverUrl}>{apiBaseUrl}</Text>
          ) : (
            <SkeletonBox width={220} height={19} style={styles.serverUrlSkeleton} />
          )}
        </View>
        <View style={styles.actions}>
          <Pressable accessibilityLabel="Refresh accounts" onPress={() => void load(true)} style={styles.iconButton}>
            <RefreshCw size={18} color="#0f172a" />
          </Pressable>
          <Link href="/settings" asChild>
            <Pressable accessibilityLabel="Open settings" style={styles.iconButton}>
              <Settings size={18} color="#0f172a" />
            </Pressable>
          </Link>
        </View>
      </View>

      {showSkeleton ? (
        <DashboardSkeleton />
      ) : (
        <>
          <View style={styles.summary}>
            <Text style={styles.summaryValue}>{accounts.length}</Text>
            <Text style={styles.summaryLabel}>active accounts</Text>
          </View>

          {error ? <StateBlock title="Server request failed" detail={error} onRetry={() => void load(true)} /> : null}
          {!loading && !error && accounts.length === 0 ? (
            <StateBlock title="No accounts found" detail="The server returned an empty account list." />
          ) : null}

          {accounts.map((account) => (
            <Link key={account.id} href={`/account/${account.id}`} asChild>
              <Pressable style={styles.card}>
                <View style={styles.cardText}>
                  <Text style={styles.cardTitle}>{account.name}</Text>
                  <Text style={styles.cardMeta}>{account.institution}</Text>
                  <Text style={styles.cardMeta}>{account.currencyCode}</Text>
                </View>
                <ChevronRight size={20} color="#64748b" />
              </Pressable>
            </Link>
          ))}
        </>
      )}
    </Screen>
  );
}

function DashboardSkeleton() {
  return (
    <>
      <View style={styles.summary}>
        <SkeletonBox width={48} height={41} style={styles.darkSkeleton} />
        <SkeletonBox width={112} height={17} style={styles.darkSkeleton} />
      </View>
      {Array.from({ length: 4 }).map((_, index) => (
        <View key={index} style={styles.card}>
          <View style={styles.cardText}>
            <SkeletonBox width="70%" height={22} />
            <SkeletonBox width="45%" height={17} />
            <SkeletonBox width={38} height={17} />
          </View>
          <SkeletonBox width={20} height={20} radius={10} />
        </View>
      ))}
    </>
  );
}

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    alignItems: 'center',
  },
  eyebrow: {
    color: '#64748b',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  serverUrl: {
    color: '#0f172a',
    fontSize: 16,
    fontWeight: '700',
  },
  serverUrlSkeleton: {
    marginTop: 1,
  },
  actions: {
    flexDirection: 'row',
    gap: 8,
  },
  iconButton: {
    width: 42,
    height: 42,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  summary: {
    padding: 18,
    borderRadius: 8,
    backgroundColor: '#0f172a',
  },
  summaryValue: {
    color: '#ffffff',
    fontSize: 34,
    fontWeight: '800',
  },
  summaryLabel: {
    color: '#cbd5e1',
    fontWeight: '700',
  },
  darkSkeleton: {
    backgroundColor: '#334155',
  },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    borderRadius: 8,
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  cardText: {
    flex: 1,
    gap: 4,
  },
  cardTitle: {
    color: '#0f172a',
    fontSize: 18,
    fontWeight: '800',
  },
  cardMeta: {
    color: '#475569',
  },
});
