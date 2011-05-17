/* Generated By:JavaCC: Do not edit this line. UParserTokenManager.java */
/*

 Copyright (c) 1998-2008 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_3
 COPYRIGHTENDKEY
 */

package ptolemy.data.unit;

public class UParserTokenManager implements UParserConstants {
    public java.io.PrintStream debugStream = System.out;

    public void setDebugStream(java.io.PrintStream ds) {
        debugStream = ds;
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
        default:
            return -1;
        }
    }

    private final int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }

    private final int jjStartNfaWithStates_0(int pos, int kind, int state) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            return pos + 1;
        }
        return jjMoveNfa_0(state, pos + 1);
    }

    private final int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
        case 36:
            return jjStartNfaWithStates_0(0, 12, 11);
        case 40:
            return jjStopAtPos(0, 20);
        case 41:
            return jjStopAtPos(0, 21);
        case 42:
            return jjStopAtPos(0, 7);
        case 43:
            return jjStopAtPos(0, 5);
        case 45:
            return jjStopAtPos(0, 6);
        case 47:
            return jjStopAtPos(0, 8);
        case 59:
            return jjStopAtPos(0, 13);
        case 60:
            return jjStopAtPos(0, 11);
        case 61:
            return jjStopAtPos(0, 10);
        case 94:
            return jjStopAtPos(0, 9);
        default:
            return jjMoveNfa_0(0, 0);
        }
    }

    private final void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }

    private final void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private final void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }

    private final void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    private final int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        jjnewStateCnt = 28;
        int i = 1;
        jjstateSet[0] = startState;
        int kind = 0x7fffffff;
        for (;;) {
            if (++jjround == 0x7fffffff) {
                ReInitRounds();
            }
            if (curChar < 64) {
                long l = 1L << curChar;
                MatchLoop: do {
                    switch (jjstateSet[--i]) {
                    case 0:
                        if ((0x3ff000000000000L & l) != 0L) {
                            jjCheckNAddStates(0, 6);
                        } else if (curChar == 36) {
                            jjstateSet[jjnewStateCnt++] = 11;
                        } else if (curChar == 46) {
                            jjCheckNAdd(3);
                        }
                        if ((0x3fe000000000000L & l) != 0L) {
                            if (kind > 14) {
                                kind = 14;
                            }
                            jjCheckNAdd(1);
                        }
                        break;
                    case 1:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 14) {
                            kind = 14;
                        }
                        jjCheckNAdd(1);
                        break;
                    case 2:
                        if (curChar == 46) {
                            jjCheckNAdd(3);
                        }
                        break;
                    case 3:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 16) {
                            kind = 16;
                        }
                        jjCheckNAddStates(7, 9);
                        break;
                    case 5:
                        if ((0x280000000000L & l) != 0L) {
                            jjCheckNAdd(6);
                        }
                        break;
                    case 6:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 16) {
                            kind = 16;
                        }
                        jjCheckNAddTwoStates(6, 7);
                        break;
                    case 9:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 17) {
                            kind = 17;
                        }
                        jjstateSet[jjnewStateCnt++] = 9;
                        break;
                    case 10:
                        if (curChar == 36) {
                            jjstateSet[jjnewStateCnt++] = 11;
                        }
                        break;
                    case 12:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 19) {
                            kind = 19;
                        }
                        jjstateSet[jjnewStateCnt++] = 12;
                        break;
                    case 13:
                        if ((0x3ff000000000000L & l) != 0L) {
                            jjCheckNAddStates(0, 6);
                        }
                        break;
                    case 14:
                        if ((0x3ff000000000000L & l) != 0L) {
                            jjCheckNAddTwoStates(14, 15);
                        }
                        break;
                    case 15:
                        if (curChar == 46) {
                            jjCheckNAdd(16);
                        }
                        break;
                    case 16:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 16) {
                            kind = 16;
                        }
                        jjCheckNAddStates(10, 12);
                        break;
                    case 18:
                        if ((0x280000000000L & l) != 0L) {
                            jjCheckNAdd(19);
                        }
                        break;
                    case 19:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 16) {
                            kind = 16;
                        }
                        jjCheckNAddTwoStates(19, 7);
                        break;
                    case 20:
                        if ((0x3ff000000000000L & l) != 0L) {
                            jjCheckNAddTwoStates(20, 21);
                        }
                        break;
                    case 22:
                        if ((0x280000000000L & l) != 0L) {
                            jjCheckNAdd(23);
                        }
                        break;
                    case 23:
                        if ((0x3ff000000000000L & l) == 0L) {
                            break;
                        }
                        if (kind > 16) {
                            kind = 16;
                        }
                        jjCheckNAddTwoStates(23, 7);
                        break;
                    case 24:
                        if ((0x3ff000000000000L & l) != 0L) {
                            jjCheckNAddStates(13, 15);
                        }
                        break;
                    case 26:
                        if ((0x280000000000L & l) != 0L) {
                            jjCheckNAdd(27);
                        }
                        break;
                    case 27:
                        if ((0x3ff000000000000L & l) != 0L) {
                            jjCheckNAddTwoStates(27, 7);
                        }
                        break;
                    default:
                        break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                MatchLoop: do {
                    switch (jjstateSet[--i]) {
                    case 0:
                    case 8:
                        if ((0x7fffffe87fffffeL & l) == 0L) {
                            break;
                        }
                        if (kind > 17) {
                            kind = 17;
                        }
                        jjCheckNAddTwoStates(8, 9);
                        break;
                    case 4:
                        if ((0x2000000020L & l) != 0L) {
                            jjAddStates(16, 17);
                        }
                        break;
                    case 7:
                        if ((0x5000000050L & l) != 0L && kind > 16) {
                            kind = 16;
                        }
                        break;
                    case 9:
                        if ((0x7fffffe87fffffeL & l) == 0L) {
                            break;
                        }
                        if (kind > 17) {
                            kind = 17;
                        }
                        jjCheckNAdd(9);
                        break;
                    case 11:
                        if ((0x7fffffe87fffffeL & l) == 0L) {
                            break;
                        }
                        if (kind > 19) {
                            kind = 19;
                        }
                        jjCheckNAddTwoStates(11, 12);
                        break;
                    case 12:
                        if ((0x7fffffe87fffffeL & l) == 0L) {
                            break;
                        }
                        if (kind > 19) {
                            kind = 19;
                        }
                        jjCheckNAdd(12);
                        break;
                    case 17:
                        if ((0x2000000020L & l) != 0L) {
                            jjAddStates(18, 19);
                        }
                        break;
                    case 21:
                        if ((0x2000000020L & l) != 0L) {
                            jjAddStates(20, 21);
                        }
                        break;
                    case 25:
                        if ((0x2000000020L & l) != 0L) {
                            jjAddStates(22, 23);
                        }
                        break;
                    default:
                        break;
                    }
                } while (i != startsAt);
            } else {
                MatchLoop: do {
                    switch (jjstateSet[--i]) {
                    default:
                        break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 28 - (jjnewStateCnt = startsAt))) {
                return curPos;
            }
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }

    static final int[] jjnextStates = { 14, 15, 20, 21, 24, 25, 7, 3, 4, 7, 16,
            17, 7, 24, 25, 7, 5, 6, 18, 19, 22, 23, 26, 27, };

    public static final String[] jjstrLiteralImages = { "", null, null, null,
            null, "\53", "\55", "\52", "\57", "\136", "\75", "\74", "\44",
            "\73", null, null, null, null, null, null, "\50", "\51", };

    public static final String[] lexStateNames = { "DEFAULT", };

    static final long[] jjtoToken = { 0x3b7fe1L, };

    static final long[] jjtoSkip = { 0x1eL, };

    protected SimpleCharStream input_stream;

    private final int[] jjrounds = new int[28];

    private final int[] jjstateSet = new int[56];

    protected char curChar;

    public UParserTokenManager(SimpleCharStream stream) {
        if (SimpleCharStream.staticFlag) {
            throw new Error(
                    "ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
        }
        input_stream = stream;
    }

    public UParserTokenManager(SimpleCharStream stream, int lexState) {
        this(stream);
        SwitchTo(lexState);
    }

    public void ReInit(SimpleCharStream stream) {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }

    private final void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 28; i-- > 0;) {
            jjrounds[i] = 0x80000000;
        }
    }

    public void ReInit(SimpleCharStream stream, int lexState) {
        ReInit(stream);
        SwitchTo(lexState);
    }

    public void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : "
                    + lexState + ". State unchanged.",
                    TokenMgrError.INVALID_LEXICAL_STATE);
        } else {
            curLexState = lexState;
        }
    }

    protected Token jjFillToken() {
        Token t = Token.newToken(jjmatchedKind);
        t.kind = jjmatchedKind;
        String im = jjstrLiteralImages[jjmatchedKind];
        t.image = (im == null) ? input_stream.GetImage() : im;
        t.beginLine = input_stream.getBeginLine();
        t.beginColumn = input_stream.getBeginColumn();
        t.endLine = input_stream.getEndLine();
        t.endColumn = input_stream.getEndColumn();
        return t;
    }

    int curLexState = 0;

    int defaultLexState = 0;

    int jjnewStateCnt;

    int jjround;

    int jjmatchedPos;

    int jjmatchedKind;

    public Token getNextToken() {
        Token matchedToken;
        int curPos = 0;

        EOFLoop: for (;;) {
            try {
                curChar = input_stream.BeginToken();
            } catch (java.io.IOException e) {
                jjmatchedKind = 0;
                matchedToken = jjFillToken();
                return matchedToken;
            }

            try {
                input_stream.backup(0);
                while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L) {
                    curChar = input_stream.BeginToken();
                }
            } catch (java.io.IOException e1) {
                continue EOFLoop;
            }
            jjmatchedKind = 0x7fffffff;
            jjmatchedPos = 0;
            curPos = jjMoveStringLiteralDfa0_0();
            if (jjmatchedKind != 0x7fffffff) {
                if (jjmatchedPos + 1 < curPos) {
                    input_stream.backup(curPos - jjmatchedPos - 1);
                }
                if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                    matchedToken = jjFillToken();
                    return matchedToken;
                } else {
                    continue EOFLoop;
                }
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            String error_after = null;
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (java.io.IOException e1) {
                EOFSeen = true;
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
                if (curChar == '\n' || curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else {
                    error_column++;
                }
            }
            if (!EOFSeen) {
                input_stream.backup(1);
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
            }
            throw new TokenMgrError(EOFSeen, curLexState, error_line,
                    error_column, error_after, curChar,
                    TokenMgrError.LEXICAL_ERROR);
        }
    }

}
