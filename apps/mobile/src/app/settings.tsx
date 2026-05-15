import { Download, Save } from 'lucide-react-native';
import { useEffect, useState } from 'react';
import { Linking, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { Screen } from '@/components/Screen';
import { defaultApiBaseUrl, getApiBaseUrl, setApiBaseUrl } from '@/lib/settings';

const releasesUrl = 'https://github.com/paradoxon-tools/plannr-server/releases/latest';

export default function SettingsScreen() {
  const [value, setValue] = useState(defaultApiBaseUrl);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    void getApiBaseUrl().then(setValue);
  }, []);

  async function save() {
    await setApiBaseUrl(value);
    setSaved(true);
    setTimeout(() => setSaved(false), 1800);
  }

  return (
    <Screen>
      <View style={styles.card}>
        <Text style={styles.label}>Server API base URL</Text>
        <TextInput
          autoCapitalize="none"
          autoCorrect={false}
          keyboardType="url"
          onChangeText={setValue}
          placeholder={defaultApiBaseUrl}
          style={styles.input}
          value={value}
        />
        <Pressable accessibilityRole="button" onPress={save} style={styles.primaryButton}>
          <Save size={18} color="#ffffff" />
          <Text style={styles.primaryButtonText}>{saved ? 'Saved' : 'Save'}</Text>
        </Pressable>
      </View>

      <View style={styles.card}>
        <Text style={styles.title}>App updates</Text>
        <Text style={styles.copy}>
          GitHub Actions publishes installable APK artifacts. Open the latest release from this device,
          download the APK, and install it over the current app.
        </Text>
        <Pressable accessibilityRole="link" onPress={() => Linking.openURL(releasesUrl)} style={styles.secondaryButton}>
          <Download size={18} color="#0f172a" />
          <Text style={styles.secondaryButtonText}>Latest GitHub release</Text>
        </Pressable>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 12,
    padding: 16,
    borderRadius: 8,
    backgroundColor: '#ffffff',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#cbd5e1',
  },
  label: {
    color: '#334155',
    fontWeight: '800',
  },
  title: {
    color: '#0f172a',
    fontSize: 18,
    fontWeight: '800',
  },
  copy: {
    color: '#475569',
    lineHeight: 20,
  },
  input: {
    minHeight: 48,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#94a3b8',
    paddingHorizontal: 12,
    color: '#0f172a',
  },
  primaryButton: {
    minHeight: 46,
    borderRadius: 8,
    backgroundColor: '#2563eb',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  primaryButtonText: {
    color: '#ffffff',
    fontWeight: '800',
  },
  secondaryButton: {
    minHeight: 46,
    borderRadius: 8,
    backgroundColor: '#e2e8f0',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  secondaryButtonText: {
    color: '#0f172a',
    fontWeight: '800',
  },
});
