#!/usr/bin/env ruby
require 'trollop'
require 'net/http'
require 'nokogiri'

BASE_URL = 'http://overpass-api.de/api/interpreter'

opts = Trollop::options do
    opt :lon, "longitude of lower left corner", type: :integer, required: true
    opt :lat, "latitude of lower left corner", type: :integer, required: true
    opt :width, "width of area for which to download features, in degrees", default: 0.1
    opt :height, "height of area for which to download features, in degrees", default: 0.1
end


req = "#{BASE_URL}?data=node(#{opts.lat}, #{opts.lon}, #{opts.lat + opts.height}, #{opts.lon + opts.width})[natural=peak];out;"
puts req
doc = Nokogiri::HTML(Net::HTTP.get('stackoverflow.com', '/index.html'))
p doc