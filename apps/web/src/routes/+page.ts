import { env } from '$env/dynamic/public';

export const load = () => ({
  apiBaseUrl: env.PUBLIC_API_BASE_URL || 'http://localhost:8080'
});
