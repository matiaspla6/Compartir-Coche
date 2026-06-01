package compartircoche;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


/**
 * Aplicación de consola para compartir coche
 *
 * @author oaguado
 *
 *         Funcionalidades:
 *         - Iniciar sesión
 *         - Publicar viaje
 *         - Solicitar asiento
 */
public class AppCompartirCoche {
    static Scanner sc = new Scanner(System.in);;


    /**
     * Main del programa
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("*************************");
        System.out.println("* COMPARTIR COCHE v.0.1 *");
        System.out.println("*************************");
        // Iniciar sesión
        String usuario = iniciarSesion();


        // Menú principal
        int opcionUsuario;
        do {
            System.out.println("*******************");
            System.out.println("* COMPARTIR COCHE *");
            System.out.println("*******************");
            System.out.println("Usuario: " + usuario + "\n");
            System.out.println("1. Publicar Viaje como conductor/a");
            System.out.println("2. Solicitar asiento como pasajero/a");
            System.out.println("0. SALIR");
            opcionUsuario = sc.nextInt();
            sc.nextLine();


            switch (opcionUsuario) {
                case 1:
                    publicarViaje(usuario);
                    break;
                case 2:
                    solicitarAsiento(usuario);
                    break;
                case 0:
                    System.out.println("Hasta pronto");
                    break;
                default:
                    System.out.println("Opción incorrecta");
            }
        } while (opcionUsuario != 0);


        System.out.println("Fin del programa");
        sc.close();
    }


    /**
     * Publica un viaje
     *
     * @param usuario Usuario que publica el viaje como conductor/a
     */
    private static void publicarViaje(String usuario) {
        System.out.print("Escribe la fecha y hora del viaje (AAAA-MM-DD HH:MM): ");
        String fechaHora = sc.nextLine();
        System.out.print("Lugar de origen: ");
        String origen = sc.nextLine();
        System.out.print("Lugar de destino: ");
        String destino = sc.nextLine();
        System.out.print("Número de plazas libres: ");
        int plazas = sc.nextInt();
        sc.nextLine();


        if (crearViaje(usuario, fechaHora, origen, destino, plazas)) {
            System.out.println("Viaje creado");
        } else {
            System.out.println("Error al crear el viaje");
        }
    }


    /**
     * Solicita un asiento en un viaje
     *
     * @param usuario Usuario que solicita el asiento
     */
    private static void solicitarAsiento(String usuario) {
        int numViajesProximos = listarProximosViajes();
        if (numViajesProximos > 0) {
            System.out.print("Indica el ID del viaje que quieres solicitar: ");
            int numViaje = sc.nextInt();
            sc.nextLine();
            if (addPasajero(numViaje, usuario)) {
                System.out.println("Asiento solicitado");
            } else {
                System.out.println("Error al solicitar el asiento");
            }
        } else {
            System.out.println("No hay viajes próximos con plazas libres");
        }
    }


    /**
     * Inicia sesión de usuario
     * Solicita credenciales de inicio de sesión, y si son correctas devuelve el
     * nombre de usuario.
     *
     * @return Usuario que ha iniciado sesión
     */
    public static String iniciarSesion() {
        do {
            System.out.println("LOGIN DE USUARIO");
            System.out.print("Usuario: ");
            String usuario = System.console().readLine();
            System.out.print("Contraseña: ");
            String password = new String(System.console().readPassword());
            if (loginUsuario(usuario, password)) {
                return usuario;
            } else {
                System.out.println("Usuario o contraseña incorrectos");
            }
        } while (true);
    }


    /**
     * Comprueba si un usuario y contraseña son correctos
     *
     * @param username Usuario
     * @param password Contraseña
     * @return true si el usuario y contraseña son correctos
     */
    public static boolean loginUsuario(String username, String password) {
        boolean loginOk = false;
        Connection conexion = conectarBD();


        Statement sentencia;
        try {
            sentencia = conexion.createStatement();


            ResultSet resultado = sentencia.executeQuery("SELECT * FROM user WHERE username LIKE '" + username + "'");


            if (resultado.next()) {
                // Si existe el usuario valida la contraseña
                loginOk = password.equals(resultado.getString("password"));


            }


            resultado.close();
            sentencia.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loginOk;
    }


    /**
     * Crea un nuevo viaje
     *
     * @param usuario   Usuario que crea el viaje / conductor/a
     * @param fechaHora Fecha y hora del viaje
     * @param origen    Lugar de origen
     * @param destino   Lugar de destino
     * @param plazas    Número de plazas libres
     * @return true si se ha creado correctamente
     */
    public static boolean crearViaje(String usuario, String fechaHora, String origen, String destino, int plazas) {
        Connection conexion = conectarBD();
        Statement sentencia;
        try {
            sentencia = conexion.createStatement();
            sentencia.executeUpdate("INSERT INTO viaje (usuario, fecha_hora, origen, destino, plazas) VALUES ('"
                    + usuario + "', '" + fechaHora + "', '" + origen + "', '" + destino + "', " + plazas + ")");
            sentencia.close();
            conexion.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Lista los próximos viajes con plazas libres
     *
     * @return Número de viajes listados
     */
    public static int listarProximosViajes() {
        Connection conexion = conectarBD();
        Statement sentencia;
        try {
            sentencia = conexion.createStatement();
            // TODO: Modificar la consulta para que tenga en cuenta los pasajeros ya
            // añadidos
            sentencia.executeQuery("SELECT * FROM viaje WHERE fecha_hora > NOW() AND plazas > 0");
            ResultSet resultado = sentencia.getResultSet();
            int numRegistros = 0;
            if (resultado != null) {
                // Imprime cabecera
                System.out.println("Viajes disponibles:");
                System.out.println("ID\tUsuario\tFecha y hora\t\tPlazas\tOrigen\tDestino");
                while (resultado.next()) {
                    // Obtiene los datos del registro actual
                    int id = resultado.getInt("id");
                    String usuario = resultado.getString("usuario");
                    String fechaHora = resultado.getString("fecha_hora");
                    String origen = resultado.getString("origen");
                    String destino = resultado.getString("destino");
                    int plazas = resultado.getInt("plazas");


                    // Procesa los datos
                    System.out.println(id + "\t" + usuario + "\t" + fechaHora
                            + "\t" + plazas + "\t" + origen + "\t" + destino);
                    numRegistros++;
                }
            }
            resultado.close();
            sentencia.close();
            conexion.close();
            return numRegistros;
        } catch (Exception e) {
            return -1;
        }
    }


    /**
     * Añade un pasajero a un viaje
     *
     * @param numViaje Número de viaje
     * @param usuario  Usuario pasajero/a
     * @return true si se ha añadido correctamente
     */
    public static boolean addPasajero(int numViaje, String usuario) {
        Connection conexion = conectarBD();
        Statement sentencia;
        try {
            sentencia = conexion.createStatement();
            sentencia.executeUpdate(
                    "INSERT INTO pasajero (viaje, usuario) VALUES (" + numViaje + ", '" + usuario + "')");
            sentencia.close();
            conexion.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Conecta con la base de datos
     *
     * @return Conexión con la base de datos
     */
    public static Connection conectarBD() {
        // Datos de conexión a la base de datos de Desarrollo (localhost)
        final String HOST = "localhost";
        final String DATABASE = "compartircoche";
        final String USER = "root";
        final String PASSWORD = "";
        final String PORT = "3306";


        Connection con = null;


        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;


        try {
            con = DriverManager.getConnection(url, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error al conectar con la BD.");
        }


        return con;
    }
}