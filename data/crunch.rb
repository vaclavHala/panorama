filename = ARGV[0]

orig_lines = Integer(ARGV[1])
orig_cells = Integer(ARGV[2])

chunk_lines = Integer(ARGV[3])
chunk_cells = Integer(ARGV[4])

offset_x = Integer(ARGV[5])
offset_y = Integer(ARGV[6])

chunks_high = orig_lines / chunk_lines
chunks_wide = orig_cells / chunk_cells

chunks = Array.new(chunks_high) {Array.new(chunks_wide, nil)}
chunks_high.times do | row |
    chunks_wide.times do | col |
        glob_x = (col+offset_x).to_s
        glob_y = ((chunks_high-1-row)+offset_y).to_s
        chunks[row][col] = (File.new("elev_"+glob_x+"_"+glob_y, "w"))
    end
end

File.open(filename, "r") do |file|
    # Skip header
    1.upto(6) { file.gets }

    orig_lines.times do | row |
        (orig_cells - 1).times do | col |
            cell = [0, (Integer (file.gets ' ').strip)].max
            chunks[row / chunk_lines][col / chunk_cells].write([cell].pack("S"))
        end 
        cell = [0, (Integer (file.gets "\n").strip)].max
        chunks[row / chunk_lines][(orig_lines-1) / chunk_cells].write([cell].pack("S"))
    end
end

exit

r = (orig_lines / chunk_lines) - 1
chunks.each do | row |
    c = 0
    row.each do | chunk |
        p (c+offset_x).to_s + (r+offset_y).to_s + chunk.to_s
        File.open("elev_"+(c+offset_x)+(r+offset_y), "w") do |file|
            file.write([])
        end
        c += 1
    end
    r -= 1
end