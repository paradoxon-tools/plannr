import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import React from 'react';
import { useColorScheme } from 'react-native';

export default function TabLayout() {
  const colorScheme = useColorScheme();
  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: colorScheme === 'dark' ? '#111827' : '#f8fafc' },
          headerTintColor: colorScheme === 'dark' ? '#f8fafc' : '#0f172a',
          contentStyle: { backgroundColor: colorScheme === 'dark' ? '#0f172a' : '#f8fafc' },
        }}
      >
        <Stack.Screen name="index" options={{ title: 'Plannr' }} />
        <Stack.Screen name="settings" options={{ title: 'Server' }} />
        <Stack.Screen name="account/[id]" options={{ title: 'Account' }} />
        <Stack.Screen name="pocket/[id]" options={{ title: 'Pocket' }} />
      </Stack>
    </ThemeProvider>
  );
}
