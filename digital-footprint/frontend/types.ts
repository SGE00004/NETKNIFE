import type { CheckStatus } from '../../shared/types/checkStatus';

export interface MetadataFinding {
  category: string;
  value: string;
  status: CheckStatus;
  explanation: string;
}

export interface FileMetadataReport {
  id: number;
  analyzedAt: string;
  originalFilename: string | null;
  fileType: string | null;
  overallStatus: CheckStatus;
  findings: MetadataFinding[];
}

export interface DiscoveredSubdomain {
  subdomain: string;
  ipAddress: string | null;
  status: CheckStatus;
}

export interface WhoisSummary {
  registrar: string | null;
  createdDate: string | null;
  expiresDate: string | null;
}

export interface GeoLocation {
  ipAddress: string;
  country: string | null;
  city: string | null;
  isp: string | null;
  lat: number | null;
  lon: number | null;
}

export interface DomainFootprintReport {
  id: number;
  analyzedAt: string;
  domain: string;
  subdomains: DiscoveredSubdomain[];
  whois: WhoisSummary | null;
  geo: GeoLocation | null;
  overallStatus: CheckStatus;
}
