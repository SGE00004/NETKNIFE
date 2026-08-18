import { createContext, useContext, type ReactNode } from 'react';

/**
 * Categoria de color de la interfaz. 'yellow' es el color por defecto (escaner
 * de red y cualquier pantalla neutral); 'red' y 'blue' corresponden a las
 * herramientas registradas como equipo rojo/azul en shared/config/tools.ts.
 */
export type ThemeCategory = 'yellow' | 'red' | 'blue';

const ThemeCategoryContext = createContext<ThemeCategory>('yellow');

interface ThemeProviderProps {
  category: ThemeCategory;
  children: ReactNode;
}

/**
 * Pone el atributo data-theme en un div que envuelve a children: las variables
 * CSS de shared/index.css leen ese atributo y recolorean automaticamente todo
 * lo que ya use las clases cyber-* de Tailwind, sin que ningun componente
 * necesite saber en que equipo esta. Futuras herramientas heredan el color solo
 * con declarar su categoria al registrarse en shared/config/tools.ts.
 */
export function ThemeProvider({ category, children }: ThemeProviderProps) {
  return (
    <ThemeCategoryContext.Provider value={category}>
      <div data-theme={category}>{children}</div>
    </ThemeCategoryContext.Provider>
  );
}

export function useThemeCategory(): ThemeCategory {
  return useContext(ThemeCategoryContext);
}
