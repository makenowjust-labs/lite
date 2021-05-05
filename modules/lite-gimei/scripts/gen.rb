# A `Data.scala` generator.

require 'yaml'

def titlecase(s)
  s.gsub(/(?:^|_)([a-z])/) { $1.upcase }
end

addresses = YAML.load_file("#{__dir__}/../data/addresses.yml")
names = YAML.load_file("#{__dir__}/../data/names.yml")

puts 'package codes.quine.labo.lite.gimei'
puts
puts '/** Data contains auto generated data constants. */'
puts 'object Data {'

%w(prefecture city town).each do |key|
  data = addresses['addresses'][key]
  puts
  puts "  /** All possible `#{key}` data. */"
  puts "  final lazy val #{titlecase(key)}: IndexedSeq[Word] = Word.load("
  puts '    Seq('
  puts "      #{data.each_slice(100).map { |slice| slice.map { |vs| vs.join(' ') }.join("\n").inspect }.join(" + \"\\n\",\n      ")}"
  puts '    ).mkString'
  puts '  )'
end

[['last_name'], ['first_name', 'male'], ['first_name', 'female']].each do |keys|
  data = names.dig(*keys)
  puts
  puts "  /** All possible #{[keys[1], "`#{keys[0]}`"].compact.join(" ")} data. */"
  puts "  final lazy val #{keys.reverse.map { |s| titlecase(s) }.join}: IndexedSeq[Word] = Word.load("
  puts '    Seq('
  puts "      #{data.each_slice(100).map { |slice| slice.map { |vs| vs.join(' ') }.join("\n").inspect }.join(" + \"\\n\",\n      ")}"
  puts '    ).mkString'
  puts '  )'
end

puts '}'
