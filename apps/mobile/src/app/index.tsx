import { Link, useFocusEffect } from 'expo-router';
import { ChevronRight, RefreshCw, Settings } from 'lucide-react-native';
import { useCallback, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Screen } from '@/components/Screen';
import { StateBlock } from '@/components/StateBlock';
import { Account, api } from '@/lib/api';
import { getApiBaseUrl } from '@/lib/settings';

export default function DashboardScreen() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [apiBaseUrl, setApiBaseUrl] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [baseUrl, accountData] = await Promise.all([getApiBaseUrl(), api.listAccounts()]);
      setApiBaseUrl(baseUrl);
      setAccounts(accountData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load accounts');
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <Screen>
      <View style={styles.header}>
        <View>
          <Text style={styles.eyebrow}>Server</Text>
          <Text style={styles.serverUrl}>{apiBaseUrl || 'Loading...'}</Text>
        </View>
        <View style={styles.actions}>
          <Pressable accessibilityLabel="Refresh accounts" onPress={load} style={styles.iconButton}>
            <RefreshCw size={18} color="#0f172a" />
          </Pressable>
          <Link href="/settings" asChild>
            <Pressable accessibilityLabel="Open settings" style={styles.iconButton}>
              <Settings size={18} color="#0f172a" />
            </Pressable>
          </Link>
        </View>
      </View>

      <View style={styles.summary}>
        <Text style={styles.summaryValue}>{accounts.length}</Text>
        <Text style={styles.summaryLabel}>active accounts</Text>
      </View>

      {loading ? <StateBlock title="Loading accounts" loading /> : null}
      {error ? <StateBlock title="Server request failed" detail={error} onRetry={load} /> : null}
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
    </Screen>
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
