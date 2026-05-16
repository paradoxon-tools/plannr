import { StyleSheet, View, ViewStyle } from 'react-native';

type SkeletonBoxProps = {
  width?: ViewStyle['width'];
  height: number;
  radius?: number;
  style?: ViewStyle;
};

export function SkeletonBox({ width = '100%', height, radius = 6, style }: SkeletonBoxProps) {
  return <View style={[styles.box, { width, height, borderRadius: radius }, style]} />;
}

const styles = StyleSheet.create({
  box: {
    backgroundColor: '#e2e8f0',
  },
});
