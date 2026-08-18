/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './shared/**/*.{js,ts,jsx,tsx}', './*/frontend/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        risk: {
          green: '#39ff8a',
          yellow: '#f5d90a',
          red: '#ff2e4d',
          neutral: '#7c8a99',
        },
        cyber: {
          bg: '#08080b',
          panel: '#101014',
          panelAlt: '#17171d',
          border: '#2a2a33',
          // Los 4 tokens "accent" de abajo (borderBright/yellow/yellowBright/yellowDim) leen de
          // variables CSS definidas en shared/index.css por atributo data-theme (yellow/red/blue),
          // en vez de un hex fijo: es lo que hace que TODO componente que ya usa clases
          // cyber-yellow/glow-yellow se vuelva theme-reactivo automaticamente sin tocarlo,
          // segun en que categoria (equipo rojo/azul/neutral) este registrada la herramienta
          // activa. Los nombres se mantienen ("yellow") por compatibilidad con el codigo
          // existente aunque puedan renderizar rojo o azul.
          borderBright: 'rgb(var(--cyber-borderBright-rgb) / <alpha-value>)',
          yellow: 'rgb(var(--cyber-yellow-rgb) / <alpha-value>)',
          yellowBright: 'rgb(var(--cyber-yellowBright-rgb) / <alpha-value>)',
          yellowDim: 'rgb(var(--cyber-yellowDim-rgb) / <alpha-value>)',
          text: '#e7e7e2',
          textDim: '#8f8f99',
        },
      },
      fontFamily: {
        mono: ['"Cascadia Code"', 'Consolas', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      boxShadow: {
        'glow-yellow': '0 0 6px rgb(var(--cyber-yellow-rgb) / 0.55), 0 0 22px rgb(var(--cyber-yellow-rgb) / 0.2)',
        'glow-yellow-sm': '0 0 4px rgb(var(--cyber-yellow-rgb) / 0.5)',
        'glow-green': '0 0 6px rgba(57,255,138,0.55), 0 0 22px rgba(57,255,138,0.2)',
        'glow-red': '0 0 6px rgba(255,46,77,0.55), 0 0 22px rgba(255,46,77,0.2)',
        'glow-neutral': '0 0 6px rgba(124,138,153,0.5), 0 0 18px rgba(124,138,153,0.18)',
      },
      keyframes: {
        flicker: {
          '0%, 100%': { opacity: '1' },
          '92%': { opacity: '1' },
          '93%': { opacity: '0.4' },
          '94%': { opacity: '1' },
          '96%': { opacity: '0.6' },
          '97%': { opacity: '1' },
        },
      },
      animation: {
        flicker: 'flicker 6s infinite',
      },
    },
  },
  plugins: [],
};
