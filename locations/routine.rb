s = (copy paste)
a = s.split("\n")
a.each do |place|
  p '<option value="' + place.strip + '">' + place + '</option>'
end
