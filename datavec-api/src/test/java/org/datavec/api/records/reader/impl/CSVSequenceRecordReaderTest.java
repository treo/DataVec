/*
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.api.records.reader.impl;

import org.datavec.api.records.SequenceRecord;
import org.datavec.api.records.metadata.RecordMetaData;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.InputSplit;
import org.datavec.api.util.ClassPathResource;
import org.datavec.api.writable.Writable;
import org.junit.Test;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CSVSequenceRecordReaderTest {

    @Test
    public void test() throws Exception {

        CSVSequenceRecordReader seqReader = new CSVSequenceRecordReader(1,",");
        seqReader.initialize(new TestInputSplit());

        int sequenceCount = 0;
        while(seqReader.hasNext()){
            List<List<Writable>> sequence = seqReader.sequenceRecord();
            assertEquals(4,sequence.size());    //4 lines, plus 1 header line

            Iterator<List<Writable>> timeStepIter = sequence.iterator();
            int lineCount = 0;
            while(timeStepIter.hasNext()){
                List<Writable> timeStep = timeStepIter.next();
                assertEquals(3,timeStep.size());
                Iterator<Writable> lineIter = timeStep.iterator();
                int countInLine = 0;
                while(lineIter.hasNext()){
                    Writable entry = lineIter.next();
                    int expValue = 100*sequenceCount + 10*lineCount + countInLine;
                    assertEquals(String.valueOf(expValue),entry.toString());
                    countInLine++;
                }
                lineCount++;
            }
            sequenceCount++;
        }
    }

    @Test
    public void testReset() throws Exception {
        CSVSequenceRecordReader seqReader = new CSVSequenceRecordReader(1,",");
        seqReader.initialize(new TestInputSplit());

        int nTests = 5;
        for( int i=0; i<nTests; i++ ) {
            seqReader.reset();

            int sequenceCount = 0;
            while (seqReader.hasNext()) {
                List<List<Writable>> sequence = seqReader.sequenceRecord();
                assertEquals(4, sequence.size());    //4 lines, plus 1 header line

                Iterator<List<Writable>> timeStepIter = sequence.iterator();
                int lineCount = 0;
                while (timeStepIter.hasNext()) {
                    timeStepIter.next();
                    lineCount++;
                }
                sequenceCount++;
                assertEquals(4,lineCount);
            }
            assertEquals(3,sequenceCount);
        }
    }

    @Test
    public void testMetaData() throws Exception {
        CSVSequenceRecordReader seqReader = new CSVSequenceRecordReader(1,",");
        seqReader.initialize(new TestInputSplit());

        List<List<List<Writable>>> l = new ArrayList<>();
        while (seqReader.hasNext()) {
            List<List<Writable>> sequence = seqReader.sequenceRecord();
            assertEquals(4, sequence.size());    //4 lines, plus 1 header line

            Iterator<List<Writable>> timeStepIter = sequence.iterator();
            int lineCount = 0;
            while (timeStepIter.hasNext()) {
                timeStepIter.next();
                lineCount++;
            }
            assertEquals(4,lineCount);

            l.add(sequence);
        }

        List<SequenceRecord> l2 = new ArrayList<>();
        List<RecordMetaData> meta = new ArrayList<>();
        seqReader.reset();
        while(seqReader.hasNext()){
            SequenceRecord sr = seqReader.nextSequence();
            l2.add(sr);
            meta.add(sr.getMetaData());
        }
        assertEquals(3, l2.size());

        List<SequenceRecord> fromMeta = seqReader.loadSequenceFromMetaData(meta);
        for( int i=0; i<3; i++ ){
            assertEquals(l.get(i), l2.get(i).getSequenceRecord());
            assertEquals(l.get(i), fromMeta.get(i).getSequenceRecord());
        }
    }

    private static class TestInputSplit implements InputSplit {

        @Override
        public long length() {
            return 3;
        }

        @Override
        public URI[] locations() {
            URI[] arr = new URI[3];
            try {
                arr[0] = new ClassPathResource("csvsequence_0.txt").getFile().toURI();
                arr[1] = new ClassPathResource("csvsequence_1.txt").getFile().toURI();
                arr[2] = new ClassPathResource("csvsequence_2.txt").getFile().toURI();
            } catch(Exception e ){
                throw new RuntimeException(e);
            }
            return arr;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public double toDouble(){
            throw new UnsupportedOperationException();
        }

        @Override
        public float toFloat(){
            throw new UnsupportedOperationException();
        }

        @Override
        public int toInt(){
            throw new UnsupportedOperationException();
        }

        @Override
        public long toLong(){
            throw new UnsupportedOperationException();
        }
    }
}
