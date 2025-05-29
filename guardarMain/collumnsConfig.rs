use clap::Parser;

/// Application configuration parsed from CLI arguments
#[derive(Parser, Debug)]
#[command(name = "Lua File Scanner")]
#[command(about = "Scans files with a Lua script and generates reports.")]
pub struct AppConfig {
    /// Path to the Lua script to run
    #[arg(short, long, default_value = "file_data_gatherer.lua")]
    pub script_path: String,
}