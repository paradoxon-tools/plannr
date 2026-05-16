import { RefreshCw } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

type StateBlockProps = {
  title: string;
  detail?: string;
  onRetry?: () => void;
};

export function StateBlock({ title, detail, onRetry }: StateBlockProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>{title}</Text>
      {detail ? <Text style={styles.detail}>{detail}</Text> : null}
      {onRetry ? (
        <Pressable accessibilityRole="button" onPress={onRetry} style={styles.button}>
          <RefreshCw size={16} color="#ffffff" />
          <Text style={styles.buttonText}>Retry</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 10,
    alignItems: 'center',
    padding: 20,
    borderRadius: 8,
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  title: {
    color: '#0f172a',
    fontSize: 18,
    fontWeight: '700',
    textAlign: 'center',
  },
  detail: {
    color: '#475569',
    textAlign: 'center',
  },
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    borderRadius: 8,
    backgroundColor: '#2563eb',
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  buttonText: {
    color: '#ffffff',
    fontWeight: '700',
  },
});
