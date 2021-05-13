# A `Data.scala` generator.

grapheme_break_property = File.read("#{__dir__}/../data/GraphemeBreakProperty.txt").lines(chomp: true)
                              .filter { |line| !line.start_with?('#') && line != '' }
emoji_data = File.read("#{__dir__}/../data/emoji-data.txt").lines(chomp: true)
                 .filter { |line| !line.start_with?('#') && line != '' }

data = []

grapheme_break_property.each do |line|
  info, comment = line.split('#', 2).map(&:strip)
  range, value = info.split(';', 2).map(&:strip)

  case range
  when /(\h+)\.\.(\h+)/
    data << [$1.to_i(16), $2.to_i(16), value, comment]
  when /(\h+)/
    data << [$1.to_i(16), $1.to_i(16), value, comment]
  end
end

emoji_data.each do |line|
  info, comment = line.split('#', 2).map(&:strip)
  range, value = info.split(';', 2).map(&:strip)
  next unless value == 'Extended_Pictographic'

  case range
  when /(\h+)\.\.(\h+)/
    data << [$1.to_i(16), $2.to_i(16), value, comment]
  when /(\h+)/
    data << [$1.to_i(16), $1.to_i(16), value, comment]
  end
end

data.sort!

puts 'package codes.quine.labo.lite.grapheme'
puts
puts 'import codes.quine.labo.lite.grapheme.Property._'
puts
puts '/** Data contains auto generated data constants. */'
puts 'object Data {'

puts '  final val CodePoints: IndexedSeq[(Int, Int, Property)] = IndexedSeq[(Int, Int, Property)]('
data.each do |i, j, value, comment|
  puts "    (0x#{i.to_s(16)}, 0x#{j.to_s(16)}, #{value}), // #{comment}"
end
puts '  )'

puts '}'
