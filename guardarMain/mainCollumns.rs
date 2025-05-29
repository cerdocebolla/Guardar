mod config;
mod lua_runner;
mod logger;
mod utils;

use crate::config::AppConfig;
use crate::lua_runner::LuaExecutor;
use crate::logger::setup_logger;
use crate::utils::file_exists;
use clap::Parser;

fn main() {
    setup_logger();

    let config = AppConfig::parse();

    log::info!("📁 Starting Lua File Scanner");

    if !file_exists(&config.script_path) {
        log::error!("Lua script not found at: {}", config.script_path);
        std::process::exit(1);
    }

    let lua_executor = LuaExecutor::new(&config.script_path);

    match lua_executor.execute() {
        Ok(_) => {
            log::info!("No viruses has been detected inside this file. You're good to go.");
        }
        Err(e) => {
            log::error!("This file is not safe. Remove it instantly: {}", e);
            std::process::exit(1);
        }
    }

    log::info!("The Guardar File Scanner has reported the issues successfully.");
}