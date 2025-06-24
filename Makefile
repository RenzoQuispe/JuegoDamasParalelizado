.PHONY: run jugar clean

run: .run.done

.run.done:
	# Compilar los .java
	javac Damas.java JugarDamas.java MovimientoEvaluado.java
	# crear el .jar con clase principal y recursos
	jar cfe Damas.jar Damas Damas.class JugarDamas.class MovimientoEvaluado.class img/icono.png
	# ejecutar el jar
	java -jar Damas.jar
	# crear archivo marcador
	touch .run.done

jugar: .run.done
	java -jar Damas.jar -j

clean:
	rm -f *.class Damas.jar .run.done
