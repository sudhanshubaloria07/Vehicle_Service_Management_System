#!/usr/bin/env bash
# =====================================================================
# Vehicle Service Management System (VSMS) - Linux / macOS Build Script
# =====================================================================

set -e

echo "====================================================================="
echo "          AUTOCARE VEHICLE SERVICE MANAGEMENT SYSTEM (VSMS)          "
echo "              B.Tech CSE 2nd Year - OOPJ Semester Project            "
echo "====================================================================="
echo ""

# Create output directory
mkdir -p bin

echo "[1/2] Compiling Java source files..."
javac -encoding UTF-8 -cp "lib/*" -d bin \
    src/service/exception/*.java \
    src/service/vehicle/*.java \
    src/service/billing/*.java \
    src/service/core/*.java \
    src/service/database/*.java \
    src/service/gui/*.java

echo "[1/2] Compilation successful!"
echo ""
echo "[2/2] Launching AutoCare VSMS Application..."
echo ""

# Classpath on Linux/Mac uses colon (:) separator
java -cp "bin:lib/*" service.core.MainApp
