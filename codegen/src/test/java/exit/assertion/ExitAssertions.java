package exit.assertion;

import java.security.Permission;

import static java.lang.System.getSecurityManager;
import static java.lang.System.setSecurityManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public enum ExitAssertions {
    ;

    public static <E extends Throwable> void assertExits(final int expectedStatus, final ThrowingExecutable<E> executable) throws E {
        final SecurityManager originalSecurityManager = getSecurityManager();
        setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(final Permission perm) {
                if (originalSecurityManager != null)
                    originalSecurityManager.checkPermission(perm);
            }

            @Override
            public void checkPermission(final Permission perm, final Object context) {
                if (originalSecurityManager != null)
                    originalSecurityManager.checkPermission(perm, context);
            }

            @Override
            public void checkExit(final int status) {
                super.checkExit(status);
                throw new ExitException(status);
            }
        });
        try {
            executable.run();
            fail("Expected System.exit(" + expectedStatus + ") to be called, but it wasn't called.");
        } catch (final ExitException e) {
            assertEquals(expectedStatus, e.status, "Wrong System.exit() status.");
        } finally {
            setSecurityManager(originalSecurityManager);
        }
    }

    public interface ThrowingExecutable<E extends Throwable> {
        void run() throws E;
    }

    private static class ExitException extends SecurityException {
        final int status;

        private ExitException(final int status) {
            this.status = status;
        }
    }
}
