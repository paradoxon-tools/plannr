import { ChevronLeft, ChevronRight } from 'lucide-react-native';
import { useEffect } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';

export type PrototypeVariant = {
  key: string;
  name: string;
};

type PrototypeSwitcherProps = {
  current: string;
  variants: PrototypeVariant[];
  onChange: (key: string) => void;
};

export function PrototypeSwitcher({ current, variants, onChange }: PrototypeSwitcherProps) {
  const currentIndex = Math.max(0, variants.findIndex((variant) => variant.key === current));

  const cycle = (direction: -1 | 1) => {
    const nextIndex = (currentIndex + direction + variants.length) % variants.length;
    onChange(variants[nextIndex].key);
  };

  useEffect(() => {
    if (Platform.OS !== 'web') {
      return;
    }

    const onKeyDown = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null;
      const tagName = target?.tagName?.toLowerCase();
      if (tagName === 'input' || tagName === 'textarea' || target?.isContentEditable) {
        return;
      }
      if (event.key === 'ArrowLeft') {
        cycle(-1);
      }
      if (event.key === 'ArrowRight') {
        cycle(1);
      }
    };

    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  });

  if (!__DEV__) {
    return null;
  }

  const active = variants[currentIndex];

  return (
    <View style={styles.bar} accessibilityLabel="Prototype variant switcher">
      <Pressable accessibilityLabel="Previous prototype" accessibilityRole="button" onPress={() => cycle(-1)} style={styles.button}>
        <ChevronLeft color="#f8fafc" size={18} />
      </Pressable>
      <View style={styles.labelWrap}>
        <Text style={styles.kicker}>PROTOTYPE</Text>
        <Text numberOfLines={1} style={styles.label}>
          {active.key} — {active.name}
        </Text>
      </View>
      <Pressable accessibilityLabel="Next prototype" accessibilityRole="button" onPress={() => cycle(1)} style={styles.button}>
        <ChevronRight color="#f8fafc" size={18} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    alignItems: 'center',
    alignSelf: 'center',
    backgroundColor: '#111318',
    borderColor: '#4b5563',
    borderRadius: 20,
    borderWidth: StyleSheet.hairlineWidth,
    bottom: 18,
    elevation: 20,
    flexDirection: 'row',
    gap: 6,
    maxWidth: 390,
    padding: 6,
    position: 'absolute',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 18,
    width: '92%',
    zIndex: 100,
  },
  button: {
    alignItems: 'center',
    backgroundColor: '#292d35',
    borderRadius: 14,
    height: 40,
    justifyContent: 'center',
    width: 40,
  },
  labelWrap: {
    alignItems: 'center',
    flex: 1,
  },
  kicker: {
    color: '#8f98a8',
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 1.2,
  },
  label: {
    color: '#f8fafc',
    fontSize: 12,
    fontWeight: '700',
  },
});
