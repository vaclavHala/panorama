filename = ARGV[0]
dir_rel = ARGV[1]
if !File.directory?(dir_rel)
    puts dir_rel+" does not exist or not directory"
    exit
end 

orig_cells = Integer(ARGV[2])
orig_degs = Integer(ARGV[3])
chunk_degs = Integer(ARGV[4])

offset_x_deg = Integer(ARGV[5])
offset_y_deg = Integer(ARGV[6])

cells_per_deg = orig_cells / orig_degs
chunk_cells = chunk_degs * cells_per_deg

offset_x_cell = offset_x_deg * cells_per_deg
offset_y_cell = offset_y_deg * cells_per_deg

chunks_side = orig_degs / chunk_degs

chunks = Array.new(chunks_side) {Array.new(chunks_side, nil)}
chunks_side.times do | row |
    chunks_side.times do | col |
        glob_x = "#{col*cells_per_deg+offset_x_cell}"
        glob_y = "#{(chunks_side-1-row)*cells_per_deg+offset_y_cell}"
        name = File.join(dir_rel, "elev_#{glob_x}_#{glob_y}")
        chunks[row][col] = (File.new(name, "w"))
    end
end

File.open(filename, "r") do |file|
    # Skip header
    1.upto(6) { file.gets }

    orig_cells.times do | row |
        (orig_cells - 1).times do | col |
            cell = [0, (Integer (file.gets ' ').strip)].max
            chunks[row / chunk_cells][col / chunk_cells].write([cell].pack("S"))
        end 
        cell = [0, (Integer (file.gets "\n").strip)].max
        chunks[row / chunk_cells][(orig_cells-1) / chunk_cells].write([cell].pack("S"))
    end
end

# exit
# 
# r = (orig_lines / chunk_lines) - 1
# chunks.each do | row |
#     c = 0
#     row.each do | chunk |
#         p (c+offset_x).to_s + (r+offset_y).to_s + chunk.to_s
#         File.open("elev_"+(c+offset_x)+(r+offset_y), "w") do |file|
#             file.write([])
#         end
#         c += 1
#     end
#     r -= 1
# end