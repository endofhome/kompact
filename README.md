[![Build Status](https://travis-ci.org/forty9er/kompact.svg?branch=master)](https://travis-ci.org/forty9er/kompact)

# Kompact

A work-in-progress bookkeeping package originally written in Java (and leaning on the [TotallyLazy](https://github.com/bodar/totallylazy) functional library), but now being ported to Kotlin.

I started writing this as a way to learn how to write a 'large' application, to cement my understanding of Java, TotallyLazy (which I was using at work), to experiment with design patterns, approaches to testing etc. The fact that it would take the form of a native application (running on the Java 8 runtime) and I would learn a bit JavaFX was appealing but not essential. Though I didn't know what I was doing when I started out, I figured that the worst thing that would happen is that I would generate a big ball of legacy code that I could spend future hours refactoring (and therefore honing those skills), which is more or less what has happened.

The name is a nod to an old MS-DOS accounting package called Compact which I used for several years working in the family business. Yes! Kompact runs on Windows XP. I've tested it. We used to run Compact on Win '98, for those of you who are intested, but I won't be testing Kompact on that...

### Dependencies

Apart from the aforementioned libraries the app also makes use of [Apache POI](https://poi.apache.org/) to read and write Excel .xls files. In order to write PDF files, it requires [LibreOffice 5](https://www.libreoffice.org/) to be installed manually by the user in the default location (this should work for Windows XP+, macOS and Linux but I don't guarantee it). I'd love to write PDFs without this dependency but I haven't found a way to do this free-of-charge. Without LibreOffice the app will work (an exception may be thrown in background but ignored) but no PDFs will be produced.