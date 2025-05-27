/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.benchmark.jmh;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;
import org.apache.lucene.util.CachedBytesRefHash;
import org.apache.lucene.util.LegacyBytesRefHash;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 3, warmups = 1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 3)
public class BytesRefHashBenchmark {

  @State(Scope.Benchmark)
  public static class ExecutionPlan {
    @Param({"10000000"})
    int dataSize;

    final int termLength = 16;
    List<BytesRef> terms;
    BytesRefHash candidate;
    LegacyBytesRefHash baseline;
    CachedBytesRefHash cached;

    @Setup(Level.Trial)
    public void setup() {
      // Generate random terms
      terms = new ArrayList<>(dataSize);

      for (int i = 0; i < dataSize; i++) {
        String uuid = UUID.randomUUID().toString().substring(0, termLength);
        terms.add(new BytesRef(uuid));
      }

      candidate = new BytesRefHash();
      cached = new CachedBytesRefHash();
      baseline = new LegacyBytesRefHash();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
      terms = null;
    }
  }

  //  @Benchmark
  //  public void candidateAdd(ExecutionPlan plan, Blackhole blackhole) {
  //    for (BytesRef term : plan.terms) {
  //      blackhole.consume(plan.candidate.add(term));
  //    }
  //  }
  //
  //  @Benchmark
  //  public void baselineAdd(ExecutionPlan plan, Blackhole blackhole) {
  //    for (BytesRef term : plan.terms) {
  //      blackhole.consume(plan.baseline.add(term));
  //    }
  //  }
  //
  //  @Benchmark
  //  public void cachedAdd(ExecutionPlan plan, Blackhole blackhole) {
  //    for (BytesRef term : plan.terms) {
  //      blackhole.consume(plan.cached.add(term));
  //    }
  //  }

  @Benchmark
  public void candidateFind(ExecutionPlan plan, Blackhole blackhole) {
    if (plan.candidate.size() == 0) {
      for (BytesRef term : plan.terms) {
        plan.candidate.add(term);
      }
    }

    for (BytesRef term : plan.terms) {
      blackhole.consume(plan.candidate.find(term));
    }
  }

  @Benchmark
  public void baselineFind(ExecutionPlan plan, Blackhole blackhole) {
    if (plan.baseline.size() == 0) {
      for (BytesRef term : plan.terms) {
        plan.baseline.add(term);
      }
    }

    for (BytesRef term : plan.terms) {
      blackhole.consume(plan.baseline.find(term));
    }
  }

  @Benchmark
  public void cachedFind(ExecutionPlan plan, Blackhole blackhole) {
    if (plan.cached.size() == 0) {
      for (BytesRef term : plan.terms) {
        plan.cached.add(term);
      }
    }

    for (BytesRef term : plan.terms) {
      blackhole.consume(plan.cached.find(term));
    }
  }

  @Benchmark
  public void candidateMixedAddFind(ExecutionPlan plan, Blackhole blackhole) {
    plan.candidate = new BytesRefHash();
    int half = plan.terms.size() / 2;

    for (int i = 0; i < half; i++) {
      blackhole.consume(plan.candidate.add(plan.terms.get(i)));
    }

    for (int i = half; i < plan.terms.size(); i++) {
      blackhole.consume(plan.candidate.add(plan.terms.get(i)));
      blackhole.consume(plan.candidate.find(plan.terms.get(i - half)));
    }
  }

  @Benchmark
  public void baselineMixedAddFind(ExecutionPlan plan, Blackhole blackhole) {
    plan.baseline = new LegacyBytesRefHash();
    int half = plan.terms.size() / 2;

    for (int i = 0; i < half; i++) {
      blackhole.consume(plan.baseline.add(plan.terms.get(i)));
    }

    for (int i = half; i < plan.terms.size(); i++) {
      blackhole.consume(plan.baseline.add(plan.terms.get(i)));
      blackhole.consume(plan.baseline.find(plan.terms.get(i - half)));
    }
  }

  @Benchmark
  public void cachedMixedAddFind(ExecutionPlan plan, Blackhole blackhole) {
    plan.cached = new CachedBytesRefHash();
    int half = plan.terms.size() / 2;

    for (int i = 0; i < half; i++) {
      blackhole.consume(plan.cached.add(plan.terms.get(i)));
    }

    for (int i = half; i < plan.terms.size(); i++) {
      blackhole.consume(plan.cached.add(plan.terms.get(i)));
      blackhole.consume(plan.cached.find(plan.terms.get(i - half)));
    }
  }
}
