// PROTOTYPE — Three application-shell and gesture-ownership variants, switchable via ?variant=.
import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import {
  ArrowDownLeft,
  ArrowLeft,
  ArrowUpRight,
  BarChart3,
  ChevronRight,
  Landmark,
  MessageCircle,
  MoreHorizontal,
  Plus,
  ReceiptText,
  ShoppingBasket,
  WalletCards,
} from 'lucide-react-native';
import { ReactNode, useEffect, useMemo } from 'react';
import {
  BackHandler,
  PanResponder,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
  ViewStyle,
  useWindowDimensions,
} from 'react-native';

import { PrototypeSwitcher, PrototypeVariant } from '@/components/PrototypeSwitcher';

const VARIANTS: PrototypeVariant[] = [
  { key: 'A', name: 'Root-only pager' },
  { key: 'B', name: 'Navigation-band pager' },
  { key: 'C', name: 'Edge-gated pager' },
];

const FEATURES = [
  { key: 'dashboard', label: 'Dashboard', icon: BarChart3 },
  { key: 'chat', label: 'Chat', icon: MessageCircle },
  { key: 'groceries', label: 'Groceries', icon: ShoppingBasket },
  { key: 'finances', label: 'Finances', icon: WalletCards },
] as const;

type Secondary = 'activity' | null;
type ShellProps = {
  featureIndex: number;
  secondary: Secondary;
  onFeatureChange: (index: number) => void;
  onSecondaryChange: (secondary: Secondary) => void;
};

export default function ShellPrototypeRoute() {
  const params = useLocalSearchParams<{ variant?: string; feature?: string; layer?: string }>();
  const router = useRouter();
  const variant = VARIANTS.some((candidate) => candidate.key === params.variant) ? params.variant! : 'A';
  const featureIndex = Math.max(0, FEATURES.findIndex((feature) => feature.key === params.feature));
  const secondary: Secondary = params.layer === 'activity' ? 'activity' : null;

  const navigate = (next: { variant?: string; featureIndex?: number; secondary?: Secondary }) => {
    const nextVariant = next.variant ?? variant;
    const nextFeature = FEATURES[next.featureIndex ?? featureIndex].key;
    const nextSecondary = next.secondary === undefined ? secondary : next.secondary;
    router.replace({
      pathname: '/prototype-shell',
      params: {
        variant: nextVariant,
        feature: nextFeature,
        ...(nextSecondary ? { layer: nextSecondary } : {}),
      },
    } as never);
  };

  const shellProps: ShellProps = {
    featureIndex,
    secondary,
    onFeatureChange: (index) => navigate({ featureIndex: index, secondary: null }),
    onSecondaryChange: (screen) => navigate({ secondary: screen }),
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.desktopStage}>
        <View style={styles.phone}>
          {variant === 'A' ? <VariantA {...shellProps} /> : null}
          {variant === 'B' ? <VariantB {...shellProps} /> : null}
          {variant === 'C' ? <VariantC {...shellProps} /> : null}
        </View>
      </View>
      <PrototypeSwitcher current={variant} onChange={(key) => navigate({ variant: key })} variants={VARIANTS} />
    </SafeAreaView>
  );
}

function VariantA({ featureIndex, secondary, onFeatureChange, onSecondaryChange }: ShellProps) {
  useBackHandler(secondary, onSecondaryChange);
  const pager = usePagerGesture(secondary === null, featureIndex, onFeatureChange);

  if (secondary) {
    return (
      <View style={styles.shell}>
        <SecondaryScreen onBack={() => onSecondaryChange(null)} treatment="takeover" />
        <StateReadout featureIndex={featureIndex} layer="activity" owner="secondary stack · pager locked" />
      </View>
    );
  }

  return (
    <View style={styles.shell} {...pager.panHandlers}>
      <View style={styles.aHeader}>
        <Text style={styles.brand}>PLANNR</Text>
        <FeatureLabels activeIndex={featureIndex} onChange={onFeatureChange} treatment="top" />
      </View>
      <FeaturePane featureIndex={featureIndex} onOpenActivity={() => onSecondaryChange('activity')} />
      <View style={styles.swipeHint} pointerEvents="none">
        <Text style={styles.swipeHintText}>SWIPE PAGE</Text>
      </View>
      <StateReadout featureIndex={featureIndex} layer="overview" owner="full root surface · horizontal wins after intent" />
    </View>
  );
}

function VariantB({ featureIndex, secondary, onFeatureChange, onSecondaryChange }: ShellProps) {
  useBackHandler(secondary, onSecondaryChange);
  const bandPager = usePagerGesture(secondary === null, featureIndex, onFeatureChange);

  return (
    <View style={[styles.shell, styles.bShell]}>
      <View style={styles.bHeader}>
        <View>
          <Text style={styles.bBrand}>Plannr</Text>
          <Text style={styles.bCaption}>ONE PRIVATE SERVER</Text>
        </View>
        <Pressable style={styles.avatar}>
          <Text style={styles.avatarText}>C</Text>
        </Pressable>
      </View>
      <View style={styles.band} {...bandPager.panHandlers}>
        <FeatureLabels activeIndex={featureIndex} onChange={onFeatureChange} treatment="band" />
        <Text style={styles.bandHint}>drag this band to change feature</Text>
      </View>
      <FeaturePane featureIndex={featureIndex} onOpenActivity={() => onSecondaryChange('activity')} roomy />
      {secondary ? (
        <View style={styles.sheetBackdrop}>
          <View style={styles.sheet}>
            <View style={styles.sheetHandle} />
            <SecondaryScreen onBack={() => onSecondaryChange(null)} treatment="sheet" />
          </View>
        </View>
      ) : null}
      <StateReadout
        featureIndex={featureIndex}
        layer={secondary ? 'activity sheet' : 'overview'}
        owner={secondary ? 'modal sheet · pager locked' : 'navigation band only · content owns all gestures'}
      />
    </View>
  );
}

function VariantC({ featureIndex, secondary, onFeatureChange, onSecondaryChange }: ShellProps) {
  useBackHandler(secondary, onSecondaryChange);
  const { width } = useWindowDimensions();
  const edgePager = usePagerGesture(secondary === null, featureIndex, onFeatureChange, Math.min(width, 480));

  return (
    <View style={[styles.shell, styles.cShell]} {...edgePager.panHandlers}>
      <View style={styles.cTopBar}>
        <View>
          <Text style={styles.cOverline}>WORKSPACE</Text>
          <Text style={styles.cTitle}>{FEATURES[featureIndex].label}</Text>
        </View>
        <Pressable style={styles.moreButton}>
          <MoreHorizontal color="#282828" size={21} />
        </Pressable>
      </View>
      {secondary ? (
        <SecondaryScreen onBack={() => onSecondaryChange(null)} treatment="nested" />
      ) : (
        <FeaturePane featureIndex={featureIndex} onOpenActivity={() => onSecondaryChange('activity')} />
      )}
      <View pointerEvents="none" style={styles.leftEdge}>
        <Text style={styles.edgeGlyph}>‹</Text>
      </View>
      <View pointerEvents="none" style={styles.rightEdge}>
        <Text style={styles.edgeGlyph}>›</Text>
      </View>
      <View style={styles.cRail}>
        {FEATURES.map((feature, index) => {
          const Icon = feature.icon;
          const active = index === featureIndex;
          return (
            <Pressable
              accessibilityLabel={feature.label}
              accessibilityRole="button"
              disabled={secondary !== null}
              key={feature.key}
              onPress={() => onFeatureChange(index)}
              style={[styles.cRailItem, active && styles.cRailItemActive, secondary && styles.cRailItemDisabled]}
            >
              <Icon color={active ? '#f7f5ef' : '#77736b'} size={17} />
              {active ? <Text style={styles.cRailLabel}>{feature.label}</Text> : null}
            </Pressable>
          );
        })}
      </View>
      <StateReadout
        featureIndex={featureIndex}
        layer={secondary ? 'activity' : 'overview'}
        owner={secondary ? 'feature stack · rail visible but locked' : '30 px screen edges only · content owns center'}
      />
    </View>
  );
}

function FeatureLabels({
  activeIndex,
  onChange,
  treatment,
}: {
  activeIndex: number;
  onChange: (index: number) => void;
  treatment: 'top' | 'band';
}) {
  return (
    <View style={[styles.labels, treatment === 'band' && styles.bandLabels]}>
      {FEATURES.map((feature, index) => {
        const Icon = feature.icon;
        const active = index === activeIndex;
        return (
          <Pressable
            accessibilityLabel={feature.label}
            accessibilityRole="button"
            key={feature.key}
            onPress={() => onChange(index)}
            style={[
              styles.labelButton,
              treatment === 'band' && styles.bandLabelButton,
              active && (treatment === 'top' ? styles.labelButtonActive : styles.bandLabelButtonActive),
            ]}
          >
            <Icon color={active ? (treatment === 'top' ? '#101114' : '#ffffff') : '#868a91'} size={16} />
            {active ? (
              <Text style={[styles.labelText, treatment === 'band' && styles.bandLabelText]}>{feature.label}</Text>
            ) : null}
          </Pressable>
        );
      })}
    </View>
  );
}

function FeaturePane({
  featureIndex,
  onOpenActivity,
  roomy = false,
}: {
  featureIndex: number;
  onOpenActivity: () => void;
  roomy?: boolean;
}) {
  const feature = FEATURES[featureIndex];
  if (feature.key !== 'finances') {
    return (
      <ScrollView contentContainerStyle={[styles.placeholderPane, roomy && styles.roomyPane]}>
        <Text style={styles.paneNumber}>0{featureIndex + 1}</Text>
        <feature.icon color="#1b1d21" size={34} strokeWidth={1.5} />
        <Text style={styles.placeholderTitle}>{feature.label}</Text>
        <Text style={styles.placeholderCopy}>A future feature pane. Present here only to make the shell real.</Text>
        <View style={styles.fixtureTag}>
          <Text style={styles.fixtureText}>PLACEHOLDER FIXTURE</Text>
        </View>
      </ScrollView>
    );
  }

  return (
    <ScrollView contentContainerStyle={[styles.financePane, roomy && styles.roomyPane]} showsVerticalScrollIndicator={false}>
      <View style={styles.financeIntro}>
        <View>
          <Text style={styles.eyebrow}>NET POSITION</Text>
          <Text style={styles.balance}>€ 12,480.20</Text>
          <Text style={styles.delta}>+ € 640.80 this month</Text>
        </View>
        <Pressable accessibilityLabel="Add transaction entry" accessibilityRole="button" style={styles.addButton}>
          <Plus color="#ffffff" size={20} />
        </Pressable>
      </View>
      <View style={styles.metrics}>
        <Metric icon={<ArrowDownLeft color="#276340" size={17} />} label="Income" value="€ 3,240" />
        <View style={styles.metricDivider} />
        <Metric icon={<ArrowUpRight color="#9f3f36" size={17} />} label="Spent" value="€ 2,599" />
      </View>
      <Text style={styles.sectionTitle}>Accounts</Text>
      <EntityRow icon={<Landmark color="#25272b" size={19} />} label="Everyday" meta="N26 · 4 pockets" value="€ 4,821.60" />
      <EntityRow icon={<WalletCards color="#25272b" size={19} />} label="Long term" meta="ING · 2 pockets" value="€ 7,658.60" />
      <View style={styles.sectionHeadingRow}>
        <Text style={styles.sectionTitle}>Recent activity</Text>
        <Pressable accessibilityRole="button" onPress={onOpenActivity}>
          <Text style={styles.viewAll}>VIEW ALL</Text>
        </Pressable>
      </View>
      <Pressable accessibilityLabel="Open recent activity" accessibilityRole="button" onPress={onOpenActivity} style={styles.activityCard}>
        <EntityRow compact icon={<ReceiptText color="#25272b" size={18} />} label="Weekly groceries" meta="Today · Groceries" value="− € 64.20" />
        <View style={styles.rowDivider} />
        <EntityRow compact icon={<ReceiptText color="#25272b" size={18} />} label="Salary" meta="Yesterday · Income" value="+ € 3,240" />
      </Pressable>
      <View style={styles.scrollProof}>
        <Text style={styles.scrollProofTitle}>Vertical scroll stays with this pane</Text>
        <Text style={styles.scrollProofCopy}>Drag up and down here, then test the variant’s horizontal feature gesture.</Text>
      </View>
    </ScrollView>
  );
}

function SecondaryScreen({ onBack, treatment }: { onBack: () => void; treatment: 'takeover' | 'sheet' | 'nested' }) {
  return (
    <View style={[styles.secondary, treatment === 'sheet' && styles.secondarySheet]}>
      <View style={styles.secondaryHeader}>
        <Pressable accessibilityLabel="Back to Finances" accessibilityRole="button" onPress={onBack} style={styles.backButton}>
          <ArrowLeft color="#202226" size={20} />
        </Pressable>
        <View style={styles.secondaryHeading}>
          <Text style={styles.eyebrow}>{treatment === 'sheet' ? 'FOCUSED SHEET' : 'FINANCES'}</Text>
          <Text style={styles.secondaryTitle}>Recent activity</Text>
        </View>
      </View>
      <ScrollView contentContainerStyle={styles.activityList} showsVerticalScrollIndicator={false}>
        <Text style={styles.dateLabel}>TODAY</Text>
        <EntityRow icon={<ReceiptText color="#25272b" size={18} />} label="Weekly groceries" meta="Everyday · Groceries" value="− € 64.20" />
        <EntityRow icon={<ReceiptText color="#25272b" size={18} />} label="Coffee" meta="Everyday · Eating out" value="− € 4.60" />
        <Text style={styles.dateLabel}>YESTERDAY</Text>
        <EntityRow icon={<ReceiptText color="#25272b" size={18} />} label="Salary" meta="Everyday · Income" value="+ € 3,240" />
        <EntityRow icon={<ReceiptText color="#25272b" size={18} />} label="Rent" meta="Everyday · Housing" value="− € 980" />
        <EntityRow icon={<ReceiptText color="#25272b" size={18} />} label="Savings transfer" meta="Long term · Transfer" value="− € 400" />
      </ScrollView>
    </View>
  );
}

function Metric({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <View style={styles.metric}>
      {icon}
      <View>
        <Text style={styles.metricLabel}>{label}</Text>
        <Text style={styles.metricValue}>{value}</Text>
      </View>
    </View>
  );
}

function EntityRow({
  compact = false,
  icon,
  label,
  meta,
  value,
}: {
  compact?: boolean;
  icon: ReactNode;
  label: string;
  meta: string;
  value: string;
}) {
  return (
    <View style={[styles.entityRow, compact && styles.compactRow]}>
      <View style={styles.entityIcon}>{icon}</View>
      <View style={styles.entityText}>
        <Text style={styles.entityLabel}>{label}</Text>
        <Text style={styles.entityMeta}>{meta}</Text>
      </View>
      <Text style={styles.entityValue}>{value}</Text>
      {!compact ? <ChevronRight color="#a1a3a7" size={17} /> : null}
    </View>
  );
}

function StateReadout({ featureIndex, layer, owner }: { featureIndex: number; layer: string; owner: string }) {
  return (
    <View pointerEvents="none" style={styles.stateReadout}>
      <Text style={styles.stateReadoutKey}>STATE</Text>
      <Text numberOfLines={2} style={styles.stateReadoutText}>
        {FEATURES[featureIndex].label} · {layer}\nOWNER: {owner}
      </Text>
    </View>
  );
}

function usePagerGesture(enabled: boolean, index: number, onChange: (index: number) => void, edgeWidth?: number) {
  return useMemo(
    () =>
      PanResponder.create({
        onMoveShouldSetPanResponder: (_, gesture) => {
          if (!enabled) return false;
          const horizontalIntent = Math.abs(gesture.dx) > 12 && Math.abs(gesture.dx) > Math.abs(gesture.dy) * 1.35;
          if (!horizontalIntent) return false;
          if (!edgeWidth) return true;
          return gesture.x0 <= 30 || gesture.x0 >= edgeWidth - 30;
        },
        onPanResponderRelease: (_, gesture) => {
          if (gesture.dx < -42 && index < FEATURES.length - 1) onChange(index + 1);
          if (gesture.dx > 42 && index > 0) onChange(index - 1);
        },
      }),
    [edgeWidth, enabled, index, onChange],
  );
}

function useBackHandler(secondary: Secondary, onChange: (secondary: Secondary) => void) {
  useEffect(() => {
    if (Platform.OS === 'web' || !secondary) return;
    const subscription = BackHandler.addEventListener('hardwareBackPress', () => {
      onChange(null);
      return true;
    });
    return () => subscription.remove();
  }, [onChange, secondary]);
}

const shadow: ViewStyle = {
  elevation: 5,
  shadowColor: '#0a0b0d',
  shadowOffset: { width: 0, height: 3 },
  shadowOpacity: 0.08,
  shadowRadius: 12,
};

const styles = StyleSheet.create({
  safeArea: { backgroundColor: '#d9d9d7', flex: 1 },
  desktopStage: { alignItems: 'center', flex: 1, justifyContent: 'center' },
  phone: {
    backgroundColor: '#f4f3ef',
    flex: 1,
    maxHeight: 900,
    maxWidth: 480,
    overflow: 'hidden',
    width: '100%',
  },
  shell: { backgroundColor: '#f4f3ef', flex: 1, overflow: 'hidden' },
  aHeader: { backgroundColor: '#f4f3ef', gap: 14, paddingBottom: 10, paddingHorizontal: 20, paddingTop: 16 },
  brand: { color: '#17191d', fontSize: 11, fontWeight: '900', letterSpacing: 3 },
  labels: { flexDirection: 'row', gap: 6 },
  labelButton: {
    alignItems: 'center',
    borderColor: '#dad9d5',
    borderRadius: 18,
    borderWidth: StyleSheet.hairlineWidth,
    flexDirection: 'row',
    gap: 6,
    height: 36,
    justifyContent: 'center',
    paddingHorizontal: 10,
  },
  labelButtonActive: { backgroundColor: '#ffffff', borderColor: '#ffffff', ...shadow },
  labelText: { color: '#101114', fontSize: 12, fontWeight: '800' },
  financePane: { gap: 12, paddingBottom: 170, paddingHorizontal: 20, paddingTop: 12 },
  roomyPane: { paddingTop: 22 },
  financeIntro: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 10 },
  eyebrow: { color: '#777a80', fontSize: 10, fontWeight: '800', letterSpacing: 1.4 },
  balance: { color: '#17191d', fontSize: 30, fontWeight: '800', letterSpacing: -1 },
  delta: { color: '#276340', fontSize: 12, fontWeight: '700', marginTop: 3 },
  addButton: { alignItems: 'center', backgroundColor: '#17191d', borderRadius: 24, height: 46, justifyContent: 'center', width: 46 },
  metrics: { alignItems: 'center', backgroundColor: '#ffffff', borderRadius: 15, flexDirection: 'row', padding: 14, ...shadow },
  metric: { alignItems: 'center', flex: 1, flexDirection: 'row', gap: 9 },
  metricDivider: { backgroundColor: '#e3e2df', height: 32, width: StyleSheet.hairlineWidth },
  metricLabel: { color: '#85878b', fontSize: 10, fontWeight: '700' },
  metricValue: { color: '#222428', fontSize: 14, fontWeight: '800' },
  sectionTitle: { color: '#202226', fontSize: 13, fontWeight: '900', letterSpacing: 0.2, marginTop: 7 },
  sectionHeadingRow: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  viewAll: { color: '#6e7177', fontSize: 10, fontWeight: '900', letterSpacing: 0.8, marginTop: 7 },
  entityRow: { alignItems: 'center', backgroundColor: '#ffffff', borderRadius: 14, flexDirection: 'row', gap: 10, minHeight: 68, padding: 11 },
  compactRow: { borderRadius: 0, minHeight: 62, paddingHorizontal: 4 },
  entityIcon: { alignItems: 'center', backgroundColor: '#efefec', borderRadius: 11, height: 40, justifyContent: 'center', width: 40 },
  entityText: { flex: 1 },
  entityLabel: { color: '#222428', fontSize: 13, fontWeight: '800' },
  entityMeta: { color: '#818389', fontSize: 10, marginTop: 3 },
  entityValue: { color: '#222428', fontSize: 12, fontVariant: ['tabular-nums'], fontWeight: '800' },
  activityCard: { backgroundColor: '#ffffff', borderRadius: 15, paddingHorizontal: 10, ...shadow },
  rowDivider: { backgroundColor: '#ebeae7', height: StyleSheet.hairlineWidth, marginLeft: 54 },
  scrollProof: { backgroundColor: '#e8e7e3', borderRadius: 14, marginTop: 4, padding: 15 },
  scrollProofTitle: { color: '#313338', fontSize: 12, fontWeight: '800' },
  scrollProofCopy: { color: '#73767c', fontSize: 11, lineHeight: 16, marginTop: 4 },
  placeholderPane: { alignItems: 'flex-start', flexGrow: 1, justifyContent: 'center', paddingBottom: 120, paddingHorizontal: 34 },
  paneNumber: { color: '#d7d6d2', fontSize: 80, fontWeight: '300', letterSpacing: -5 },
  placeholderTitle: { color: '#1b1d21', fontSize: 34, fontWeight: '800', letterSpacing: -1, marginTop: 18 },
  placeholderCopy: { color: '#74777d', fontSize: 14, lineHeight: 20, marginTop: 8, maxWidth: 290 },
  fixtureTag: { borderColor: '#b8b8b4', borderRadius: 4, borderWidth: StyleSheet.hairlineWidth, marginTop: 24, paddingHorizontal: 8, paddingVertical: 5 },
  fixtureText: { color: '#777a80', fontSize: 9, fontWeight: '900', letterSpacing: 1.2 },
  swipeHint: { alignSelf: 'center', backgroundColor: '#e1e0dc', borderRadius: 8, bottom: 85, paddingHorizontal: 9, paddingVertical: 5, position: 'absolute' },
  swipeHintText: { color: '#888a8f', fontSize: 8, fontWeight: '900', letterSpacing: 1.3 },
  stateReadout: { backgroundColor: '#17191de8', borderRadius: 9, bottom: 78, left: 12, maxWidth: 270, paddingHorizontal: 10, paddingVertical: 7, position: 'absolute', zIndex: 50 },
  stateReadoutKey: { color: '#9ea2aa', fontSize: 7, fontWeight: '900', letterSpacing: 1.2 },
  stateReadoutText: { color: '#f4f3ef', fontSize: 9, fontWeight: '700', lineHeight: 13, marginTop: 2 },
  secondary: { backgroundColor: '#f4f3ef', flex: 1, paddingHorizontal: 20, paddingTop: 18 },
  secondarySheet: { paddingHorizontal: 18, paddingTop: 10 },
  secondaryHeader: { alignItems: 'center', flexDirection: 'row', gap: 12 },
  backButton: { alignItems: 'center', backgroundColor: '#ffffff', borderRadius: 20, height: 40, justifyContent: 'center', width: 40 },
  secondaryHeading: { flex: 1 },
  secondaryTitle: { color: '#202226', fontSize: 21, fontWeight: '800' },
  activityList: { gap: 8, paddingBottom: 150, paddingTop: 22 },
  dateLabel: { color: '#82848a', fontSize: 9, fontWeight: '900', letterSpacing: 1.3, marginBottom: 1, marginTop: 10 },
  bShell: { backgroundColor: '#eeeeec' },
  bHeader: { alignItems: 'center', backgroundColor: '#ffffff', flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 20, paddingVertical: 16 },
  bBrand: { color: '#181a1e', fontSize: 21, fontWeight: '900', letterSpacing: -0.8 },
  bCaption: { color: '#92949a', fontSize: 8, fontWeight: '800', letterSpacing: 1.3 },
  avatar: { alignItems: 'center', backgroundColor: '#1c1e22', borderRadius: 18, height: 36, justifyContent: 'center', width: 36 },
  avatarText: { color: '#ffffff', fontSize: 12, fontWeight: '900' },
  band: { backgroundColor: '#282b30', paddingBottom: 8, paddingHorizontal: 14, paddingTop: 11 },
  bandLabels: { justifyContent: 'space-between' },
  bandLabelButton: { borderColor: '#474b52', height: 38 },
  bandLabelButtonActive: { backgroundColor: '#f7f7f5', borderColor: '#f7f7f5', flexGrow: 1 },
  bandLabelText: { color: '#202226' },
  bandHint: { color: '#8d929c', fontSize: 8, fontWeight: '700', letterSpacing: 0.8, marginTop: 6, textAlign: 'center' },
  sheetBackdrop: { backgroundColor: '#00000055', bottom: 0, left: 0, paddingTop: 110, position: 'absolute', right: 0, top: 0, zIndex: 20 },
  sheet: { backgroundColor: '#f4f3ef', borderTopLeftRadius: 26, borderTopRightRadius: 26, flex: 1, overflow: 'hidden', paddingTop: 8 },
  sheetHandle: { alignSelf: 'center', backgroundColor: '#b3b3af', borderRadius: 2, height: 4, marginBottom: 2, width: 36 },
  cShell: { backgroundColor: '#f7f5ef' },
  cTopBar: { alignItems: 'center', borderBottomColor: '#dedcd5', borderBottomWidth: StyleSheet.hairlineWidth, flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 22, paddingVertical: 15 },
  cOverline: { color: '#918e86', fontSize: 8, fontWeight: '900', letterSpacing: 1.5 },
  cTitle: { color: '#282828', fontSize: 24, fontWeight: '500', letterSpacing: -0.8 },
  moreButton: { alignItems: 'center', borderColor: '#d0cec7', borderRadius: 18, borderWidth: StyleSheet.hairlineWidth, height: 36, justifyContent: 'center', width: 36 },
  cRail: { alignItems: 'center', alignSelf: 'center', backgroundColor: '#282826', borderRadius: 22, bottom: 78, flexDirection: 'row', gap: 3, padding: 5, position: 'absolute', zIndex: 40 },
  cRailItem: { alignItems: 'center', borderRadius: 17, flexDirection: 'row', gap: 6, height: 34, justifyContent: 'center', paddingHorizontal: 10 },
  cRailItemActive: { backgroundColor: '#494943' },
  cRailItemDisabled: { opacity: 0.55 },
  cRailLabel: { color: '#f7f5ef', fontSize: 10, fontWeight: '800' },
  leftEdge: { alignItems: 'flex-start', bottom: 165, justifyContent: 'center', left: 0, position: 'absolute', top: 80, width: 24 },
  rightEdge: { alignItems: 'flex-end', bottom: 165, justifyContent: 'center', position: 'absolute', right: 0, top: 80, width: 24 },
  edgeGlyph: { color: '#aaa79f', fontSize: 24, fontWeight: '300' },
});
