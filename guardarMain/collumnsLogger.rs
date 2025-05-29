use env_logger::{Builder, Target};
use log::LevelFilter;
use std::io::Write;

pub fn setup_logger() {
    Builder::new()
        .target(Target::Stdout)
        .format(|buf, record| {
            writeln!(
                buf,
                "[{}] {}: {}",
                chrono::Local::now().format("%Y-%m-%d %H:%M:%S"),
                record.level(),
                record.args()
            )
        })
        .filter(None, LevelFilter::Info)
        .init();
}
