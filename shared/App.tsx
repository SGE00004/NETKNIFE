import { useState } from 'react';
import { Layout } from './components/Layout';
import type { ToolId } from './types/toolId';
import { CryptojackingDetectorPage } from '../cryptojacking-detector/frontend/CryptojackingDetectorPage';
import { DigitalFootprintPage } from '../digital-footprint/frontend/DigitalFootprintPage';
import { ExposureCheckerPage } from '../exposure-checker/frontend/ExposureCheckerPage';
import { FindingsReportPage } from '../findings-report/frontend/FindingsReportPage';
import { HygieneChecklistPage } from '../hygiene-checklist/frontend/HygieneChecklistPage';
import { IncidentGuidePage } from '../incident-guide/frontend/IncidentGuidePage';
import { NetworkScannerPage } from '../network-scanner/frontend/NetworkScannerPage';
import { PhishingSimulatorPage } from '../phishing-simulator/frontend/PhishingSimulatorPage';
import { PortRadarPage } from '../port-radar/frontend/PortRadarPage';
import { TrafficAnalyzerPage } from '../traffic-analyzer/frontend/TrafficAnalyzerPage';
import { WifiRouterAuditPage } from '../wifi-router-audit/frontend/WifiRouterAuditPage';

function App() {
  const [activeTool, setActiveTool] = useState<ToolId>('network-scanner');

  return (
    <Layout activeTool={activeTool} onSelectTool={setActiveTool}>
      {activeTool === 'network-scanner' && <NetworkScannerPage />}
      {activeTool === 'exposure-checker' && <ExposureCheckerPage />}
      {activeTool === 'digital-footprint' && <DigitalFootprintPage />}
      {activeTool === 'port-radar' && <PortRadarPage />}
      {activeTool === 'wifi-router-audit' && <WifiRouterAuditPage />}
      {activeTool === 'findings-report' && <FindingsReportPage onNavigateToTool={setActiveTool} />}
      {activeTool === 'hygiene-checklist' && <HygieneChecklistPage />}
      {activeTool === 'incident-guide' && <IncidentGuidePage onNavigateToTool={setActiveTool} />}
      {activeTool === 'traffic-analyzer' && <TrafficAnalyzerPage />}
      {activeTool === 'cryptojacking-detector' && <CryptojackingDetectorPage />}
      {activeTool === 'phishing-simulator' && <PhishingSimulatorPage />}
    </Layout>
  );
}

export default App;
