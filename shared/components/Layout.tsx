import type { ReactNode, WheelEvent } from 'react';
import { useRef } from 'react';
import { TOOLS } from '../config/tools';
import type { ToolId } from '../types/toolId';
import { ThemeProvider } from '../theme/ThemeContext';

interface LayoutProps {
  children: ReactNode;
  activeTool: ToolId;
  onSelectTool: (toolId: ToolId) => void;
}

export function Layout({ children, activeTool, onSelectTool }: LayoutProps) {
  const activeCategory = TOOLS.find((tool) => tool.id === activeTool)?.category ?? 'yellow';
  const navScrollRef = useRef<HTMLDivElement>(null);

  const handleNavWheel = (event: WheelEvent<HTMLDivElement>) => {
    const el = navScrollRef.current;
    if (!el) return;
    if (event.deltaY === 0) return;
    el.scrollLeft += event.deltaY;
    event.preventDefault();
  };

  return (
    <ThemeProvider category={activeCategory}>
      <div className="min-h-screen bg-cyber-bg">
        <header className="relative border-b border-cyber-yellow/30 bg-black/80 backdrop-blur">
          <div className="absolute inset-x-0 bottom-0 h-px bg-gradient-to-r from-transparent via-cyber-yellow to-transparent shadow-glow-yellow-sm" />
          <div className="mx-auto flex max-w-4xl items-center gap-3 px-4 py-4">
            <span className="h-2.5 w-2.5 flex-shrink-0 bg-cyber-yellow shadow-glow-yellow animate-flicker" aria-hidden />
            <div>
              <h1 className="font-mono text-lg font-bold uppercase tracking-[0.3em] text-cyber-yellow text-glow-accent-strong">
                NETKNIFE
              </h1>
              <p className="font-mono text-xs uppercase tracking-widest text-cyber-textDim">
                Protege tu red sin ser un experto
              </p>
            </div>
          </div>
        </header>

        <nav className="border-b border-cyber-border bg-black/40">
          <div
            ref={navScrollRef}
            onWheel={handleNavWheel}
            className="mx-auto flex max-w-4xl gap-1 overflow-x-auto px-4">
            {TOOLS.map((tool) => {
              const active = tool.id === activeTool;
              return (
                <button
                  key={tool.id}
                  onClick={() => onSelectTool(tool.id)}
                  className={`whitespace-nowrap border-b-2 px-3 py-3 font-mono text-xs font-medium uppercase tracking-wide transition ${
                    active
                      ? 'border-cyber-yellow text-cyber-yellow'
                      : 'border-transparent text-cyber-textDim hover:text-cyber-text'
                  }`}
                >
                  {tool.label}
                </button>
              );
            })}
          </div>
        </nav>

        <main className="mx-auto max-w-4xl px-4 py-8">{children}</main>
      </div>
    </ThemeProvider>
  );
}
