import React from "react";
import Dashboard from "./Dashboard";

export default function App() {
  const path = window.location.pathname;

  if (path === "/dashboard") {
    return <Dashboard />;
  }

  return (
    <div style={{
      display: "flex", flexDirection: "column", alignItems: "center",
      justifyContent: "center", height: "100vh", fontFamily: "sans-serif", backgroundColor: "#f9f9f9"
    }}>
      <div style={{ padding: "40px", background: "#fff", borderRadius: "8px", boxShadow: "0 4px 12px rgba(0,0,0,0.1)", textAlign: "center" }}>
        <h2 style={{ margin: "0 0 16px 0", color: "#333" }}>CloudVandana Portal Gateway</h2>
        <p style={{ color: "#666", marginBottom: "24px" }}>Please log in to synchronize your active Salesforce sandbox workspace.</p>
        <button 
          onClick={() => window.location.href = "http://localhost:8080/api/auth/login"}
          style={{
            padding: "12px 24px", fontSize: "16px", backgroundColor: "#0070d2",
            color: "#fff", border: "none", borderRadius: "4px", cursor: "pointer", fontWeight: "bold"
          }}
        >
          Connect to Salesforce
        </button>
      </div>
    </div>
  );
}
