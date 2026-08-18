import { useState } from 'react';
import { MAX_RECIPIENTS } from '../types';

interface RecipientInputProps {
  recipients: string[];
  onChange: (recipients: string[]) => void;
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function RecipientInput({ recipients, onChange }: RecipientInputProps) {
  const [draft, setDraft] = useState('');
  const [error, setError] = useState<string | null>(null);

  const addRecipient = () => {
    const email = draft.trim();
    if (!email) return;
    if (!EMAIL_PATTERN.test(email)) {
      setError('Esa no parece una direccion de correo valida.');
      return;
    }
    if (recipients.includes(email)) {
      setError('Ese destinatario ya esta en la lista.');
      return;
    }
    if (recipients.length >= MAX_RECIPIENTS) {
      setError(`Como maximo ${MAX_RECIPIENTS} destinatarios por envio.`);
      return;
    }
    onChange([...recipients, email]);
    setDraft('');
    setError(null);
  };

  const removeRecipient = (email: string) => {
    onChange(recipients.filter((r) => r !== email));
    setError(null);
  };

  return (
    <div>
      <label className="font-mono text-sm font-medium uppercase tracking-wide text-cyber-textDim" htmlFor="recipient-input">
        Destinatarios (maximo {MAX_RECIPIENTS})
      </label>
      <div className="mt-1 flex gap-2">
        <input
          id="recipient-input"
          type="email"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              addRecipient();
            }
          }}
          placeholder="ej: familiar@example.com"
          disabled={recipients.length >= MAX_RECIPIENTS}
          className="flex-1 border border-cyber-border bg-black px-3 py-2 text-sm text-cyber-text placeholder:text-cyber-textDim focus:border-cyber-yellow focus:outline-none focus:ring-1 focus:ring-cyber-yellow disabled:opacity-50"
        />
        <button
          onClick={addRecipient}
          disabled={recipients.length >= MAX_RECIPIENTS}
          className="border border-cyber-yellow px-3 py-2 font-mono text-sm font-medium uppercase tracking-wide text-cyber-yellow transition hover:bg-cyber-yellow hover:text-black disabled:opacity-50"
        >
          Anadir
        </button>
      </div>
      {error && <p className="mt-1 text-sm text-risk-red">{error}</p>}

      {recipients.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {recipients.map((email) => (
            <span
              key={email}
              className="flex items-center gap-2 border border-cyber-border bg-cyber-panel px-3 py-1 font-mono text-xs text-cyber-text"
            >
              {email}
              <button onClick={() => removeRecipient(email)} className="text-cyber-textDim hover:text-risk-red" aria-label={`Quitar ${email}`}>
                ✕
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
