import UIKit
import MobileCoreServices
import UniformTypeIdentifiers
import CryptoKit

class ViewController: UIViewController, UIDocumentPickerDelegate {

    var resultLabel: UILabel!
    var scanButton: UIButton!
    var knownBadHashes: Set<String> = []

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        setupUI()
        loadMalwareDatabase()
    }

    func setupUI() {
        resultLabel = UILabel()
        resultLabel.translatesAutoresizingMaskIntoConstraints = false
        resultLabel.textAlignment = .center
        resultLabel.numberOfLines = 0
        resultLabel.text = "Select a file to scan."
        view.addSubview(resultLabel)

        scanButton = UIButton(type: .system)
        scanButton.translatesAutoresizingMaskIntoConstraints = false
        scanButton.setTitle("Select File", for: .normal)
        scanButton.addTarget(self, action: #selector(selectFile), for: .touchUpInside)
        view.addSubview(scanButton)

        NSLayoutConstraint.activate([
            resultLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            resultLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor, constant: -50),
            resultLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            resultLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),

            scanButton.topAnchor.constraint(equalTo: resultLabel.bottomAnchor, constant: 20),
            scanButton.centerXAnchor.constraint(equalTo: view.centerXAnchor)
        ])
    }

    @objc func selectFile() {
        let types: [UTType] = [.item]
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: types)
        picker.delegate = self
        picker.allowsMultipleSelection = false
        present(picker, animated: true, completion: nil)
    }

    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let fileURL = urls.first else { return }
        scanFile(at: fileURL)
    }

    func scanFile(at url: URL) {
        do {
            let data = try Data(contentsOf: url)
            let hash = SHA256.hash(data: data).compactMap { String(format: "%02x", $0) }.joined()
            print("File SHA-256: \(hash)")
            checkHashAgainstDatabase(hash)
        } catch {
            resultLabel.text = "Failed to read file."
        }
    }

    func checkHashAgainstDatabase(_ hash: String) {
        if knownBadHashes.contains(hash) {
            resultLabel.text = "⚠️ Threat Detected!\n\nThis file matches a known malware signature."
            resultLabel.textColor = .systemRed
        } else {
            resultLabel.text = "✅ File is clean."
            resultLabel.textColor = .systemGreen
        }
    }

    func loadMalwareDatabase() {
        // Simulated malware hashes
        knownBadHashes = [
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", // empty file
            "d41d8cd98f00b204e9800998ecf8427e", // another fake hash
            // Add more hashes here
        ]
    }
}