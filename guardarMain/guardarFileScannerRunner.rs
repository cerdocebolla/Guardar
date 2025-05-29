use mlua::{Lua, Result as LuaResult};
use std::fs;

pub struct LuaExecutor {
    pub script_path: String,
}

impl LuaExecutor {
    pub fn new(script_path: &str) -> Self {
        Self {
            script_path: script_path.to_string(),
        }
    }

    pub fn execute(&self) -> LuaResult<()> {
        let script_content = fs::read_to_string(&self.script_path)
            .map_err(|e| mlua::Error::external(format!("Failed to read Lua script: {}", e)))?;

        let lua = Lua::new();

        let globals = lua.globals();

        log::debug!("🔧 Preparing Lua environment.");

        // (You can inject Lua functions here if needed)

        log::info!("▶️ Executing Lua script...");

        lua.load(&script_content).exec()?;

        log::info!("📄 Lua script executed.");

        Ok(())
    }
}
