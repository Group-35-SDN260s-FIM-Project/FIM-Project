# FIM Security Suite

**SDN260S – Software Design II · Group 35 · Topic 3: File Integrity Monitor**

A Java tool that records a "fingerprint" (SHA-256 hash) of every file in a folder, then re-scans that folder later to detect tampering or unwanted changes. Files are reported as **UNCHANGED**, **MODIFIED**, **NEW** or **MISSING**, and all activity is written to a log.

## Why it matters

Silent changes to files (malware, accidental edits, unauthorised tampering) are hard to spot by eye. Comparing cryptographic hashes against a trusted baseline makes any change to a file's content detectable, even when its name, size or timestamp looks the same.

## How it works

1. **Create baseline** – scan a chosen folder recursively, hash every file and save the snapshot to disk (serialised).
2. **Run check** – scan the same folder again and compare the new snapshot against the saved baseline.
3. **Report** – list every change by status and record the result in the activity log.

## Requirements

- JDK 24 (the project compiles with `--enable-preview`)
- Apache NetBeans 27 or later (includes Maven)
- Windows, macOS or Linux

## Getting started

```bash
git clone https://github.com/Group-35-SDN260s-FIM-Project/FIM-Project.git
```

1. In NetBeans choose **File → Open Project** and select the cloned folder.
2. Make sure the project uses JDK 24 (**Project Properties → Build → Compile / Run**).
3. Run the project (**F6**). The main class is `my.fim_security_suite.FIM_Security_Suite`. It opens the Swing window; add the argument `--cli` to get the console menu instead.

## Running and testing

- **GUI:** run `FIM_Security_Suite` (F6 in NetBeans). Select Folder → Create Baseline → Run Check.
- **Console:** right-click `cli/CliMenu.java` and choose **Run File**, or run the project with the argument `--cli` (or `mvn exec:java -Dexec.args="--cli"`). Menu: 1 choose folder, 2 create baseline, 3 run check, 4 view log, 5 open the window, 6–9 sandbox (create and select, tamper, reset, delete), 0 exit.
- **Demo data:** run `my.fim_security_suite.demo.SandboxTool` on its own for a small menu (1 create, 2 tamper, 3 reset, 4 delete), or pass `create`, `tamper`, `reset` or `delete` as the argument. It makes a throwaway `sandbox` folder with four files (and a `sandbox_original` backup used by reset).
- **Tests:** `my.fim_security_suite.test.CoreTest` (10 checks) and `my.fim_security_suite.test.IntegrityCheckerTest` (7 tests) are plain Java programs, so run each file and read the PASS/FAIL lines. They are not JUnit tests.
- Keep `fim_baseline.ser` and `fim_activity.log` outside the monitored folder. The program refuses a folder that contains them.

Expected demo result on the sandbox: the first check shows 4 unchanged; after `tamper` it shows 2 unchanged, 1 modified (`report.txt`), 1 new (`intruder.txt`) and 1 missing (`notes.txt`).

## Branches

| Branch | Contents |
| --- | --- |
| `main` | Final submitted version: Swing GUI, console menu, core logic, sandbox tool and tests |
| `complete-project` | Branch where the final version was built and integrated (same code as `main`) |

## Design overview

The design follows the group UML diagram (`docs/FIM_Final_UML.png`) and is split into packages:

| Package | Classes |
| --- | --- |
| *(root)* | `FIM_Security_Suite` |
| `cli` | `CliMenu` |
| `controller` | `FIMController` |
| `core` | `FileScanner`, `HashUtil`, `BaselineManager`, `IntegrityChecker`, `ActivityLogger` |
| `model` | `FileRecord`, `Baseline`, `FileChange`, `FileStatus`, `ScanResult`, `LogEntry` |
| `ui` | `MainDashBoard` (+ `MainDashBoard.form`) |
| `demo` | `SandboxTool` |
| `test` | `CoreTest`, `IntegrityCheckerTest` |

Both interfaces only call `FIMController`, so the scanning and checking logic is written once and the window and console always show the same state.

## Team – Group 35

| Member | Classes |
| --- | --- |
| Wahina Hlulani Njoku | `FIM_Security_Suite`, `FIMController`, `MainDashBoard` |
| Sandiswa Mchunu | `IntegrityChecker`, `FileChange`, `FileStatus` |
| Thamsanqa Temle | `ActivityLogger`, `LogEntry` |
| Mohlodi Molelekeng | `HashUtil`, `IntegrityCheckerTest` |
| Kholofelo Robyn Rabababela | `FileScanner`, `FileRecord` |
| Olona Mlanjeni | `BaselineManager`, `Baseline` |
| Rebecca Mandlazi | `CliMenu`, `ScanResult` |
| Mixo Rikhotso | `SandboxTool`, `CoreTest` |

Lecturer: Mr S. Ekwe.
