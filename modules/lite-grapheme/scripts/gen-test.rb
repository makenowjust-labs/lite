# A `GraphemeBreakTest.scala` generator.

def char_size(cp)
  cp >= 0x10000 ? 2 : 1
end

grapheme_break_test = File.read("#{__dir__}/../data/GraphemeBreakTest.txt").lines(chomp: true)
                          .filter { |line| !line.start_with?('#') && !line.empty? }

data = []

grapheme_break_test.each do |line|
  expected, comment = line.split('#').map(&:strip)
  expected = expected.split('÷').map(&:strip).filter{ |s| !s.empty? }.map { |s| s.split(' × ').map { |t| t.to_i(16) } }
  data << [expected, comment]
end

puts 'package codes.quine.labo.lite.grapheme'
puts
puts 'class GraphemeBreakTestSuite extends munit.FunSuite {'
puts '  def s(seq: Int*): String = seq.flatMap(Character.toChars(_).toSeq).mkString'
puts
puts '  def iterate(s: String): Iterator[Grapheme] ='
puts '    Iterator.unfold(0) { i =>'
puts '      if (i < s.length) {'
puts '        val j = Grapheme.findNextBoundary(s, i)'
puts '        Some((Grapheme(s.slice(i, j)), j))'
puts '      } else None'
puts '    }'
puts
puts '  def iterateReverse(s: String): Iterator[Grapheme] ='
puts '    Iterator.unfold(s.length) { i =>'
puts '      if (i > 0) {'
puts '        val j = Grapheme.findPreviousBoundary(s, i)'
puts '        Some((Grapheme(s.slice(j, i)), j))'
puts '      } else None'
puts '    }'

data.each do |(s, comment)|
  puts
  puts "  test(#{comment.inspect}) {"
  puts "    val str = s(#{s.flatten.join(', ')})"
  puts
  puts '    assertEquals('
  puts '      Grapheme.iterate(str).toSeq,'
  puts "      Seq(#{s.map { |cs| "Grapheme(s(#{cs.join(', ')}))" }.join(',')})"
  puts '    )'
  puts
  puts '    assertEquals('
  puts '      iterate(str).toSeq,'
  puts "      Seq(#{s.map { |cs| "Grapheme(s(#{cs.join(', ')}))" }.join(',')})"
  puts '    )'
  puts
  puts '    assertEquals('
  puts '      iterateReverse(str).toSeq.reverse,'
  puts "      Seq(#{s.map { |cs| "Grapheme(s(#{cs.join(', ')}))" }.join(',')})"
  puts '    )'
  puts
  s.reduce(0) do |index, cs|
    size = cs.map { |cp| char_size(cp) }.sum
    0.upto(size - 1) do |offset|
      puts "    assertEquals(Grapheme.findNextBoundary(str, #{index + offset}), #{index + size})"
    end
    size.downto(1) do |offset|
      puts "    assertEquals(Grapheme.findPreviousBoundary(str, #{index + offset}), #{index})"
    end
    index + size
  end
  puts '  }'
end

puts '}'
