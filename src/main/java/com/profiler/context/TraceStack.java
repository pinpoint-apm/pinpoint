package com.profiler.context;

import com.profiler.util.NamedThreadLocal;

public class TraceStack {

	private static final class Local {

		private static final ThreadLocal<State[]> context = new NamedThreadLocal<State[]>("StateContext");
		private static volatile int size = 0;

		static int add() {
			size += 1;
			return size - 1;
		}

		static void set(int i, State value) {
			assert i < size;

			State[] ctx = context.get();

			if (ctx == null) {
				ctx = new State[size];
			} else {
				State[] oldCtx = ctx;
				ctx = new State[size];
				System.arraycopy(oldCtx, 0, ctx, 0, oldCtx.length);
			}

			ctx[i] = value;
			context.set(ctx);
		}

		static State get(int i) {
			State[] ctx = context.get();

			if (ctx == null || ctx.length <= i) {
				return null;
			}

			return ctx[i];
		}

		static void clear(int i) {
			set(i, null);
		}
	}

	private final int me;

	public TraceStack() {
		me = Local.add();
	}

	public void set(State state) {
		Local.set(me, state);
	}

	public void clear() {
		Local.clear(me);
	}

	public State get() {
		return Local.get(me);
	}
}
