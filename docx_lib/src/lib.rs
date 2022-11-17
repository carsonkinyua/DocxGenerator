mod docx_glue;
use std::{fs::File, io::Read};
use rifgen::rifgen_attr::*;

use docx_rs::{Docx, Paragraph, Pic, Run};

#[cfg(target_os = "android")]
use android_logger::Config;
#[cfg(target_os = "android")]
use log::Level;

pub use crate::docx_glue::*;

pub struct RustLog;

impl RustLog {
    //set up logging
    #[generate_interface]
    pub fn initialise_logging() {
        #[cfg(target_os = "android")]
        android_logger::init_once(
            Config::default()
                .with_min_level(Level::Trace)
                .with_tag("Rust"),
        );
        log_panics::init();
        log::info!("Logging initialised from Rust");
    }
}

/// A simple android doc builder
#[generate_interface_doc]
pub struct AndroidDocBuilder {
    doc: Docx,
}

impl AndroidDocBuilder {
    #[generate_interface(constructor)]
    pub fn new() -> AndroidDocBuilder {
        AndroidDocBuilder { doc: Docx::new() }
    }

    /// Add a text to the doc
    #[generate_interface]
    pub fn add_text(&mut self, text: &str) {
        log::debug!("Adding Text {}", text);
        self.doc = self
            .doc.clone()
            .add_paragraph(Paragraph::new().add_run(Run::new().add_text(text)));
    }

    /// Add Image to the doc
    #[generate_interface]
    pub fn add_image(&mut self, file: &str, width: u32, height: u32) {
        log::debug!("Fetching file: {}", file);
        let mut img = File::open(file).unwrap();
        let mut buf = Vec::new();
        let _ = img.read_to_end(&mut buf).unwrap();

        let pic = Pic::new(buf).size(width, height);
        self.doc = self
            .doc.clone()
            .add_paragraph(Paragraph::new().add_run(Run::new().add_image(pic)));
    }

    /// Export the file
    #[generate_interface]
    pub fn generate_docx(&mut self, file_name: &str) {
        log::debug!("Exporting to {}", file_name);
        let path = std::path::Path::new(file_name);
        let file = std::fs::File::create(&path).unwrap();
        self.doc.build().pack(file).unwrap();
    }
}
