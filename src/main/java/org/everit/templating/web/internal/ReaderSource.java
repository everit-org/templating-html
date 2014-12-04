package org.everit.templating.web.internal;

import java.io.IOException;
import java.io.Reader;

import org.htmlparser.lexer.Source;
import org.htmlparser.util.ParserException;

public class ReaderSource extends Source {

    private final StringBuilder buffer = new StringBuilder();

    private boolean closed = false;

    private int mark = 0;

    private int offset = 0;

    private final Reader reader;

    public ReaderSource(final Reader reader) {
        this.reader = reader;
    }

    @Override
    public int available() {
        if (closed) {
            return 0;
        }
        return buffer.length() - offset;
    }

    @Override
    public void close() throws IOException {
        destroy();

    }

    @Override
    public void destroy() throws IOException {
        closed = true;
        reader.close();

    }

    @Override
    public char getCharacter(final int offset) throws IOException {
        return buffer.charAt(offset);
    }

    @Override
    public void getCharacters(final char[] array, final int offset, final int start, final int end) throws IOException {
        buffer.getChars(start, end, array, offset);

    }

    @Override
    public void getCharacters(final StringBuffer outputBuffer, final int offset, final int length) throws IOException {
        char[] chars = new char[length];
        buffer.getChars(offset, offset + length, chars, 0);
        outputBuffer.append(chars);

    }

    @Override
    public String getEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(final int offset, final int length) throws IOException {
        buffer.substring(offset, offset + length);
        return null;
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        mark = offset;

    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int read() throws IOException {
        int result;
        if (offset < buffer.length()) {
            result = buffer.charAt(offset);
        } else {
            result = reader.read();
            buffer.append((char) result);
        }
        offset++;
        return result;
    }

    @Override
    public int read(final char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        int left = len;
        int remainingInBuffer = buffer.length() - offset;
        int availableFromBuffer = Math.min(left, remainingInBuffer);
        if (availableFromBuffer > 0) {
            left -= availableFromBuffer;
            buffer.getChars(offset, offset + availableFromBuffer, cbuf, 0);
            offset += availableFromBuffer;
        }
        int r = 0;
        if (left > 0) {
            r = reader.read(cbuf, availableFromBuffer, left);
            if (r > -1) {
                left -= r;
            }
        }
        if (r < 0 && availableFromBuffer == 0) {
            return r;
        }

        return len - left;
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public void reset() {
        offset = mark;

    }

    @Override
    public void setEncoding(final String characterSet) throws ParserException {
        throw new UnsupportedOperationException();

    }

    @Override
    public long skip(final long n) throws IOException {
        throw new UnsupportedOperationException("Skipping is not supported");
    }

    @Override
    public void unread() throws IOException {
        if (offset == 0) {
            throw new IOException("Can't undo characters");
        }
        offset--;

    }

}
