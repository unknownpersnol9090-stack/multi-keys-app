/**
 * Mobile Bridge - Handles communication between web app and native Android shell
 * Supports FCM push notifications and native capabilities
 */

interface AndroidBridge {
  getFCMToken(): string | null;
  requestFCMToken(): void;
}

declare global {
  interface Window {
    AndroidBridge?: AndroidBridge;
    setFCMToken?: (token: string) => void;
  }
}

/**
 * Initialize mobile bridge and FCM token handling
 */
export function initializeMobileBridge() {
  // Check if running in Android WebView
  const isAndroid = /Android/.test(navigator.userAgent);
  
  if (!isAndroid) {
    console.log('Not running on Android');
    return;
  }

  // Set up FCM token handler
  window.setFCMToken = (token: string) => {
    console.log('FCM Token received:', token);
    // Store token in localStorage for later use
    localStorage.setItem('fcm_token', token);
    // Dispatch custom event for app to listen to
    window.dispatchEvent(new CustomEvent('fcm-token-updated', { detail: { token } }));
  };

  // Request FCM token from native bridge
  if (window.AndroidBridge) {
    try {
      window.AndroidBridge.requestFCMToken();
    } catch (error) {
      console.error('Failed to request FCM token:', error);
    }
  }
}

/**
 * Get stored FCM token
 */
export function getFCMToken(): string | null {
  return localStorage.getItem('fcm_token');
}

/**
 * Handle back button on Android
 */
export function setupBackButtonHandler(callback?: () => void) {
  if (window.history && window.history.length > 1) {
    window.addEventListener('popstate', () => {
      if (callback) {
        callback();
      }
    });
  }
}

/**
 * Optimize performance for mobile
 */
export function optimizeMobilePerformance() {
  // Disable pinch zoom to improve scrolling performance
  const viewport = document.querySelector('meta[name="viewport"]');
  if (viewport) {
    viewport.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover');
  }

  // Reduce motion for better performance on low-end devices
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (prefersReducedMotion) {
    document.documentElement.style.setProperty('--animation-duration', '0.01ms');
  }

  // Enable passive event listeners for better scroll performance
  if ('addEventListener' in window) {
    const passiveSupported = (() => {
      let supported = false;
      try {
        const options: EventListenerOptions = {
          get passive() {
            supported = true;
            return false;
          },
        } as any;
        (window as any).addEventListener('test', () => {}, options);
        (window as any).removeEventListener('test', () => {}, options);
      } catch (err) {
        supported = false;
      }
      return supported;
    })();

    if (passiveSupported) {
      // Passive listeners are already supported
      console.log('Passive event listeners supported');
    }
  }
}

/**
 * Setup safe area support for notches and status bars
 */
export function setupSafeArea() {
  const html = document.documentElement;
  
  // Set CSS variables for safe area insets
  const updateSafeArea = () => {
    const top = window.visualViewport?.offsetTop || 0;
    const left = window.visualViewport?.offsetLeft || 0;
    
    html.style.setProperty('--safe-area-top', `${top}px`);
    html.style.setProperty('--safe-area-left', `${left}px`);
  };

  updateSafeArea();
  window.addEventListener('resize', updateSafeArea);
  window.addEventListener('orientationchange', updateSafeArea);
}
