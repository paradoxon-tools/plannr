import { Link, useLocalSearchParams, useNavigation } from 'expo-router';
import { ChevronRight, RefreshCw } from 'lucide-react-native';
import { useCallback, useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Screen } from '@/components/Screen';
import { SkeletonBox } from '@/components/Skeleton';
import { StateBlock } from '@/components/StateBlock';
import {
  Account,
  Contract,
  Pocket,
  TransactionFeed,
  TransactionFeedItem,
  api,
  centsToMoney,
  colorFromInt,
} from '@/lib/api';
import { CACHE_REFRESH_INTERVAL_MS, getCachedData, replaceCachedData } from '@/lib/cache';

type AccountScreenData = {
  account: Account;
  contracts: Contract[];
  pockets: Pocket[];
  feed: TransactionFeed;
};

export default function AccountScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const navigation = useNavigation();
  const accountId = Number(id);
  const [account, setAccount] = useState<Account | null>(null);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [pockets, setPockets] = useState<Pocket[]>([]);
  const [feed, setFeed] = useState<TransactionFeed | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (forceRefresh = false) => {
    const cacheKey = `account.v3.${accountId}`;
    const cachedData = await getCachedData<AccountScreenData>(cacheKey);

    if (cachedData) {
      setAccount(cachedData.data.account);
      setContracts(cachedData.data.contracts);
      setPockets(cachedData.data.pockets);
      setFeed(cachedData.data.feed);
      navigation.setOptions({ title: cachedData.data.account.name });
    }

    if (!forceRefresh && cachedData && !cachedData.isStale) {
      setError(null);
      setLoading(false);
      return;
    }

    setLoading(!cachedData);
    try {
      setError(null);
      const [accountData, contractData, pocketData, feedData] = await Promise.all([
        api.getAccount(accountId),
        api.listContracts(accountId),
        api.listPockets(accountId),
        api.getAccountFeed(accountId),
      ]);
      await replaceCachedData<AccountScreenData>(cacheKey, {
        account: accountData,
        contracts: contractData,
        pockets: pocketData,
        feed: feedData,
      });
      setAccount(accountData);
      setContracts(contractData);
      setPockets(pocketData);
      setFeed(feedData);
      navigation.setOptions({ title: accountData.name });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load account');
    } finally {
      setLoading(false);
    }
  }, [accountId, navigation]);

  useEffect(() => {
    void load();
    const intervalId = setInterval(() => {
      void load();
    }, CACHE_REFRESH_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [load]);

  if (loading && !account) {
    return <AccountSkeleton />;
  }

  if (!account) {
    return (
      <Screen>
        <StateBlock title="Account request failed" detail={error ?? 'Account not found'} onRetry={() => void load(true)} />
      </Screen>
    );
  }

  return (
    <Screen>
      <View style={styles.headerCard}>
        <View style={styles.headerTop}>
          <View>
            <Text style={styles.eyebrow}>{account.institution}</Text>
            <Text style={styles.title}>{account.name}</Text>
          </View>
          <Pressable accessibilityLabel="Refresh account" onPress={() => void load(true)} style={styles.iconButton}>
            <RefreshCw size={18} color="#ffffff" />
          </Pressable>
        </View>
        <View style={styles.metricRow}>
          <View>
            <Text style={styles.metric}>{contracts.length}</Text>
            <Text style={styles.metricLabel}>contracts</Text>
          </View>
          <View>
            <Text style={styles.metric}>{pockets.length}</Text>
            <Text style={styles.metricLabel}>pockets</Text>
          </View>
          <View>
            <Text style={styles.metric}>{centsToMoney(feed?.currentBalance, account.currencyCode)}</Text>
            <Text style={styles.metricLabel}>balance</Text>
          </View>
        </View>
      </View>

      {error ? <StateBlock title="Server request failed" detail={error} onRetry={() => void load(true)} /> : null}

      <Text style={styles.sectionTitle}>Contracts</Text>
      {contracts.length === 0 ? (
        <StateBlock title="No contracts" detail="The server returned no contracts for this account." />
      ) : null}
      {contracts.map((contract) => {
        const pocket = pockets.find((item) => item.id === contract.pocketId);
        if (!pocket) {
          return null;
        }

        return (
          <Link key={contract.id} href={`/pocket/${contract.pocketId}?accountId=${account.id}`} asChild>
            <Pressable style={styles.card}>
              <View style={[styles.swatch, { backgroundColor: colorFromInt(pocket.color) }]} />
              <View style={styles.cardText}>
                <Text style={styles.cardTitle}>{pocket.name}</Text>
                <Text style={styles.cardMeta}>
                  Balance: {centsToMoney(pocket.currentBalance, account.currencyCode)}
                </Text>
                <Text style={styles.cardMeta}>
                  Ends: {contract.expirationDate ?? 'No expiration date'}
                </Text>
              </View>
              <ChevronRight size={20} color="#64748b" />
            </Pressable>
          </Link>
        );
      })}

      <Text style={styles.sectionTitle}>Transactions</Text>
      {feed?.transactions.length === 0 ? (
        <StateBlock title="No transactions" detail="The feed does not contain transactions for this account yet." />
      ) : null}
      {feed?.transactions.map((transaction) => (
        <TransactionRow key={transaction.transactionId} transaction={transaction} currencyCode={account.currencyCode} />
      ))}
    </Screen>
  );
}

function AccountSkeleton() {
  return (
    <Screen>
      <View style={styles.headerCard}>
        <View style={styles.headerTop}>
          <View style={styles.headerTextSkeleton}>
            <SkeletonBox width={118} height={15} style={styles.darkSkeleton} />
            <SkeletonBox width={210} height={31} style={styles.darkSkeleton} />
          </View>
          <SkeletonBox width={42} height={42} radius={8} style={styles.blueSkeleton} />
        </View>
        <View style={styles.metricRow}>
          <MetricSkeleton width={52} />
          <MetricSkeleton width={44} />
          <MetricSkeleton width={104} />
        </View>
      </View>

      <SkeletonBox width={96} height={24} />
      {Array.from({ length: 3 }).map((_, index) => (
        <View key={`contract-${index}`} style={styles.card}>
          <SkeletonBox width={12} height={46} radius={4} />
          <View style={styles.cardText}>
            <SkeletonBox width="68%" height={21} />
            <SkeletonBox width="82%" height={17} />
            <SkeletonBox width="58%" height={17} />
          </View>
          <SkeletonBox width={20} height={20} radius={10} />
        </View>
      ))}

      <SkeletonBox width={124} height={24} />
      {Array.from({ length: 5 }).map((_, index) => (
        <View key={`transaction-${index}`} style={styles.transactionCard}>
          <View style={styles.transactionTop}>
            <View style={styles.transactionText}>
              <SkeletonBox width="74%" height={20} />
              <SkeletonBox width="88%" height={17} />
            </View>
            <SkeletonBox width={76} height={20} />
          </View>
          <SkeletonBox width="45%" height={17} />
        </View>
      ))}
    </Screen>
  );
}

function MetricSkeleton({ width }: { width: number }) {
  return (
    <View>
      <SkeletonBox width={width} height={24} style={styles.darkSkeleton} />
      <SkeletonBox width={62} height={17} style={styles.metricLabelSkeleton} />
    </View>
  );
}

function TransactionRow({
  transaction,
  currencyCode,
}: {
  transaction: TransactionFeedItem;
  currencyCode: string;
}) {
  const counterparty =
    transaction.partner?.name ??
    transaction.destinationPocket?.name ??
    transaction.sourcePocket?.name ??
    transaction.transferPocket?.name ??
    transaction.type;

  return (
    <View style={styles.transactionCard}>
      <View style={styles.transactionTop}>
        <View style={styles.transactionText}>
          <Text style={styles.transactionTitle}>{transaction.title}</Text>
          <Text style={styles.cardMeta}>
            {transaction.transactionDate} · {counterparty}
          </Text>
        </View>
        <Text style={[styles.transactionAmount, transaction.signedAmount < 0 ? styles.negative : styles.positive]}>
          {centsToMoney(transaction.signedAmount, currencyCode)}
        </Text>
      </View>
      <Text style={styles.cardMeta}>Balance: {centsToMoney(transaction.balanceAfter, currencyCode)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  headerCard: {
    gap: 18,
    padding: 18,
    borderRadius: 8,
    backgroundColor: '#111827',
  },
  headerTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    alignItems: 'flex-start',
  },
  headerTextSkeleton: {
    gap: 4,
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
  metricRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
  },
  metric: {
    color: '#ffffff',
    fontSize: 20,
    fontWeight: '900',
  },
  metricLabel: {
    color: '#cbd5e1',
    fontWeight: '700',
  },
  metricLabelSkeleton: {
    marginTop: 3,
    backgroundColor: '#334155',
  },
  darkSkeleton: {
    backgroundColor: '#334155',
  },
  blueSkeleton: {
    backgroundColor: '#1d4ed8',
  },
  sectionTitle: {
    color: '#0f172a',
    fontSize: 20,
    fontWeight: '900',
  },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 16,
    borderRadius: 8,
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  swatch: {
    width: 12,
    height: 46,
    borderRadius: 4,
  },
  cardText: {
    flex: 1,
    gap: 4,
  },
  cardTitle: {
    color: '#0f172a',
    fontSize: 17,
    fontWeight: '800',
  },
  cardMeta: {
    color: '#475569',
  },
  transactionCard: {
    gap: 8,
    padding: 14,
    borderRadius: 8,
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  transactionTop: {
    flexDirection: 'row',
    gap: 12,
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  transactionText: {
    flex: 1,
    gap: 4,
  },
  transactionTitle: {
    color: '#0f172a',
    fontSize: 16,
    fontWeight: '800',
  },
  transactionAmount: {
    fontSize: 16,
    fontWeight: '900',
    textAlign: 'right',
  },
  positive: {
    color: '#047857',
  },
  negative: {
    color: '#be123c',
  },
});
