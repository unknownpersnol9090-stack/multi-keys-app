import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'app.lovable.multihackskey',
  appName: 'Multi Keys App',
  webDir: 'dist/public',
  server: {
    // Load the live hosted site so the native shell always shows the latest deployed UI/backend.
    url: 'https://multihackskey.lovable.app',
    cleartext: false,
    androidScheme: 'https',
    allowNavigation: [
      'multihackskey.lovable.app',
      '*.lovable.app',
      '*.supabase.co',
    ],
  },
  android: {
    allowMixedContent: false,
    captureInput: true,
    webContentsDebuggingEnabled: false,
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 1500,
      backgroundColor: '#7c3aed',
      showSpinner: false,
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert'],
    },
  },
};

export default config;