import { VectorCanvasMapProvider } from './VectorCanvasMapProvider.js';

class MapProviderRegistry {
  constructor() {
    this.providers = new Map();
    this.defaultProvider = 'vector-canvas';

    // Register built-in vector canvas GIS provider
    this.register('vector-canvas', VectorCanvasMapProvider);
  }

  register(name, providerClass) {
    this.providers.set(name.toLowerCase(), providerClass);
  }

  get(name = this.defaultProvider) {
    const Provider = this.providers.get(name.toLowerCase());
    if (!Provider) {
      console.warn(`Map provider "${name}" not found. Falling back to "${this.defaultProvider}".`);
      const Fallback = this.providers.get(this.defaultProvider);
      return new Fallback();
    }
    return new Provider();
  }

  setDefault(name) {
    if (!this.providers.has(name.toLowerCase())) {
      throw new Error(`Cannot set unregistered provider "${name}" as default`);
    }
    this.defaultProvider = name.toLowerCase();
  }

  getAvailableProviders() {
    return Array.from(this.providers.keys());
  }
}

export const mapProviderRegistry = new MapProviderRegistry();
export default mapProviderRegistry;
