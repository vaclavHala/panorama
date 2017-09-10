filename = 'srtm_39_02.asc'

out = []

ncols = 6000
nrows = 6000
xllcorner = 10
yllcorner = 50
# we are reading top to bottom, so flip y
x0, y0 = xllcorner, yllcorner + 5
cellsize = 0.00083333333333333

def split_short (num):
    lst = []
    lst.append(num & 0xFF)
    num >>= 8
    lst.append(num & 0xFF)
    return bytes(lst[::-1])


with open(filename) as file:
    for header_line in range(6):
        print "Header: " + file.next()
    for row in range(6000):
        if row % 1200 == 0:
            for open_file in out:
                open_file.close()
            del out[:]

            chunky = y0 - (5 - row) / 1200
            for i in range(5):
                chunkx = x0 + i
                chunk_name = "chunk_%s_%s" % (chunkx, chunky)
                print "Opening chunk " + chunk_name
                out.append(open(chunk_name, 'wb'))

        line = file.next()
        for col, elev in enumerate(map(int, line.split())):
            chunk = col / 1200
            out[chunk].write(split_short(elev))
