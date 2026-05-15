import { useLocalSearchParams, useNavigation } from 'expo-router';
import { RefreshCw } from 'lucide-react-native';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Screen } from '@/components/Screen';
import { StateBlock } from '@/components/StateBlock';
import { Account, Pocket, PocketWithContract, api, centsToMoney, colorFromInt } from '@/lib/api';

export default function PocketScreen() {
  const { id, accountId } = useLocalSearchParams<{ id: string; accountId?: string }>();
  const navigation = useNavigation();
  const pocketId = Number(id);
  const parsedAccountId = accountId ? Number(accountId) : undefined;
  const [account, setAccount] = useState<Account | null>(null);
  const [pocket, setPocket] = useState<Pocket | null>(null);
  const [contract, setContract] = useState<PocketWithContract | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const pocketData = await api.getPocket(pocketId);
      const resolvedAccountId = parsedAccountId ?? pocketData.accountId;
      const [accountData, contracts] = await Promise.all([
        api.getAccount(resolvedAccountId),
        api.listContracts(resolvedAccountId),
      ]);
      const contractData = contracts.find((item) => item.id === pocketId) ?? null;
      setPocket(pocketData);
      setAccount(accountData);
      setContract(contractData);
      navigation.setOptions({ title: pocketData.name });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load pocket');
    } finally {
      setLoading(false);
    }
  }, [parsedAccountId, pocketId, navigation]);

  useEffect(() => {
    void load();
  }, [load]);

  const balance = useMemo(
    () => centsToMoney(pocket?.currentBalance, account?.currencyCode ?? 'EUR'),
    [account?.currencyCode, pocket?.currentBalance],
  );

  if (loading) {
    return (
      <Screen>
        <StateBlock title="Loading pocket" loading />
      </Screen>
    );
  }

  if (error || !pocket || !account) {
    return (
      <Screen>
        <StateBlock title="Pocket request failed" detail={error ?? 'Pocket not found'} onRetry={load} />
      </Screen>
    );
  }

  return (
    <Screen>
      <View style={styles.hero}>
        <View style={[styles.colorBar, { backgroundColor: colorFromInt(pocket.color) }]} />
        <View style={styles.heroBody}>
          <View style={styles.heroTop}>
            <View style={styles.heroText}>
              <Text style={styles.eyebrow}>{account.name}</Text>
              <Text style={styles.title}>{pocket.name}</Text>
            </View>
            <Pressable accessibilityLabel="Refresh pocket" onPress={load} style={styles.iconButton}>
              <RefreshCw size={18} color="#ffffff" />
            </Pressable>
          </View>
          <Text style={styles.balanceLabel}>Current balance</Text>
          <Text style={styles.balance}>{balance}</Text>
        </View>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Contract details</Text>
        <Detail label="Description" value={pocket.description ?? 'None'} />
        <Detail label="Partner ID" value={contract?.contractInfo.partnerId?.toString() ?? 'None'} />
        <Detail label="Signing date" value={contract?.contractInfo.signingDate ?? 'None'} />
        <Detail label="Expiration date" value={contract?.contractInfo.expirationDate ?? 'None'} />
        <Detail label="Last cancellation date" value={contract?.contractInfo.lastCancellationDate ?? 'None'} />
      </View>

      {typeof pocket.currentBalance !== 'number' ? (
        <StateBlock
          title="Balance is not available yet"
          detail="The current server DTO for /pockets and /contracts does not include currentBalance. The screen is ready to display it when the API exposes that field."
        />
      ) : null}
    </Screen>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailLabel}>{label}</Text>
      <Text style={styles.detailValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  hero: {
    borderRadius: 8,
    backgroundColor: '#111827',
    overflow: 'hidden',
  },
  colorBar: {
    height: 8,
  },
  heroBody: {
    gap: 16,
    padding: 18,
  },
  heroTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    alignItems: 'flex-start',
  },
  heroText: {
    flex: 1,
  },
  eyebrow: {
    color: '#93c5fd',
    fontWeight: '800',
    textTransform: 'uppercase',
    fontSize: 12,
  },
  title: {
    color: '#ffffff',
    fontSize: 26,
    fontWeight: '900',
  },
  iconButton: {
    width: 42,
    height: 42,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#2563eb',
  },
  balanceLabel: {
    color: '#cbd5e1',
    fontWeight: '800',
  },
  balance: {
    color: '#ffffff',
    fontSize: 28,
    fontWeight: '900',
  },
  card: {
    gap: 12,
    padding: 16,
    borderRadius: 8,
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  cardTitle: {
    color: '#0f172a',
    fontSize: 18,
    fontWeight: '900',
  },
  detailRow: {
    gap: 3,
  },
  detailLabel: {
    color: '#64748b',
    fontWeight: '800',
    fontSize: 12,
    textTransform: 'uppercase',
  },
  detailValue: {
    color: '#0f172a',
    fontSize: 16,
    fontWeight: '700',
  },
});
