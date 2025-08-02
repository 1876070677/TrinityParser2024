import React from "react";
import { SyncLoader } from "react-spinners";

export const Loader: React.FC = () => {
  return (
    <div className="loader">
        <SyncLoader />
        <p>잠시만 기다려주세요...</p>
    </div>
  );
}
