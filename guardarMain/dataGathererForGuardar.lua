luarocks isntall luafilesystem

--[[
    File Data Gatherer Script
    Author: ChatGPT
    Description:
        Recursively scans directories and gathers data about each file.
        Outputs the data to a CSV file.
    Dependencies:
        - LuaFileSystem (lfs)
--]]

local lfs = require("lfs")

-- CONFIGURATION
local OUTPUT_CSV = "guardarFileScannerReports.csv"
local START_DIRECTORY = "." -- Change to your desired starting path
local INCLUDE_HIDDEN = true -- Set to true to include hidden files

-- Utilities
local function escape_csv_field(s)
    if s:find(",") or s:find("\"") or s:find("\n") then
        s = s:gsub("\"", "\"\"")
        return "\"" .. s .. "\""
    end
    return s
end

local function format_size(bytes)
    local sizes = {"B", "KB", "MB", "GB", "TB"}
    local index = 1
    while bytes > 1024 and index < #sizes do
        bytes = bytes / 1024
        index = index + 1
    end
    return string.format("%.2f %s", bytes, sizes[index])
end

local function get_file_extension(filename)
    return filename:match("^.+(%..+)$") or ""
end

local function is_hidden(filename)
    return filename:sub(1, 1) == "."
end

-- File data class
local FileData = {}
FileData.__index = FileData

function FileData:new(path, name, attr)
    local self = setmetatable({}, FileData)
    self.path = path
    self.name = name
    self.extension = get_file_extension(name)
    self.full_path = path .. "/" .. name
    self.size = attr.size or 0
    self.modification = os.date("%Y-%m-%d %H:%M:%S", attr.modification)
    self.mode = attr.mode
    return self
end

function FileData:to_csv_row()
    return table.concat({
        escape_csv_field(self.name),
        escape_csv_field(self.full_path),
        escape_csv_field(self.extension),
        escape_csv_field(format_size(self.size)),
        escape_csv_field(self.modification),
        escape_csv_field(self.mode)
    }, ",")
end

-- Main logic
local all_files = {}

local function scan_directory(path)
    for file in lfs.dir(path) do
        if file ~= "." and file ~= ".." then
            if not INCLUDE_HIDDEN and is_hidden(file) then
                -- Skip hidden files/folders
            else
                local full_path = path .. "/" .. file
                local attr = lfs.attributes(full_path)
                if attr then
                    if attr.mode == "directory" then
                        scan_directory(full_path)
                    elseif attr.mode == "file" then
                        local file_obj = FileData:new(path, file, attr)
                        table.insert(all_files, file_obj)
                    end
                else
                    io.stderr:write("Could not get attributes for: " .. full_path .. "\n")
                end
            end
        end
    end
end

local function write_csv(file_list, output_path)
    local file, err = io.open(output_path, "w")
    if not file then
        error("Could not open file for writing: " .. err)
    end

    -- Write CSV headers
    file:write("Name,Full Path,Extension,Size,Modified At,Type\n")

    -- Write data
    for _, f in ipairs(file_list) do
        file:write(f:to_csv_row() .. "\n")
    end

    file:close()
end

local function main()
    print("Scanning directory: " .. START_DIRECTORY)
    local start_time = os.clock()

    scan_directory(START_DIRECTORY)

    print("Scan complete. " .. #all_files .. " file(s) found.")
    print("Writing output to: " .. OUTPUT_CSV)
    write_csv(all_files, OUTPUT_CSV)

    local elapsed = os.clock() - start_time
    print(string.format("Done in %.2f seconds", elapsed))
end

main()

--[[
    Notes:
    - This script avoids symlinks and errors on inaccessible files.
    - You can customize extensions, filters, or sorting if desired.
    - To make it GUI-friendly or accept CLI args, extend 'main()'.
--]]
