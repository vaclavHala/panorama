chunks_x = Integer(ARGV[0])
chunks_y = Integer(ARGV[1])
chunk_cell_side = Integer(ARGV[2])

chunks_x.times do | row |
    chunks_y.times do | col |
        glob_x = "#{col*chunk_cell_side}"
        glob_y = "#{row*chunk_cell_side}"
        name = "elev_#{glob_x}_#{glob_y}"
        File.open(name, "w") do | f |
            chunk_cell_side.times do | y |
                chunk_cell_side.times do | x |
                    # cell = 1000*col + 100*row + x + (9 - y) * 10
                    f.write([x + (9 - y)].pack("S>*"))
                end
            end
        end
    end 
end
