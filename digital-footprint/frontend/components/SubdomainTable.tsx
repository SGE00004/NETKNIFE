import { checkStatusStyle } from '../../../shared/utils/checkStatus';
import type { DiscoveredSubdomain } from '../types';

interface SubdomainTableProps {
  subdomains: DiscoveredSubdomain[];
}

export function SubdomainTable({ subdomains }: SubdomainTableProps) {
  if (subdomains.length === 0) {
    return (
      <p className="border border-dashed border-cyber-border p-4 text-center text-sm text-cyber-textDim">
        No se ha encontrado ningun subdominio de la lista curada para este dominio.
      </p>
    );
  }

  return (
    <div className="overflow-x-auto border border-cyber-border">
      <table className="w-full min-w-[420px] border-collapse font-mono text-sm">
        <thead>
          <tr className="border-b border-cyber-border text-left text-xs uppercase tracking-wide text-cyber-textDim">
            <th className="px-3 py-2">Subdominio</th>
            <th className="px-3 py-2">IP</th>
            <th className="px-3 py-2">Estado</th>
          </tr>
        </thead>
        <tbody>
          {subdomains.map((subdomain) => {
            const style = checkStatusStyle(subdomain.status);
            return (
              <tr key={subdomain.subdomain} className="border-b border-cyber-border/50 last:border-0">
                <td className="px-3 py-2 text-cyber-text">{subdomain.subdomain}</td>
                <td className="px-3 py-2 text-cyber-textDim">{subdomain.ipAddress ?? '—'}</td>
                <td className="px-3 py-2">
                  <span className={`border px-2 py-0.5 text-xs font-medium uppercase tracking-wide ${style.border} ${style.bg} ${style.text}`}>
                    {style.label}
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
