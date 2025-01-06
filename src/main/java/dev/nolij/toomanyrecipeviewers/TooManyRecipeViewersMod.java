package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.registration.RuntimeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.runtime.JEIKeyMappings;
import dev.nolij.toomanyrecipeviewers.impl.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.runtime.config.JEIConfigManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRuntimeRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.*;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class TooManyRecipeViewersMod {
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Refraction REFRACTION = new Refraction(MethodHandles.lookup());
	
	@SuppressWarnings("SameParameterValue")
	private static <T> List<T> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
		Type annotationType = Type.getType(annotationClass);
		List<ModFileScanData> allScanData = ModList.get().getAllScanData();
		Set<String> pluginClassNames = new LinkedHashSet<>();
		for (ModFileScanData scanData : allScanData) {
			Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
			for (ModFileScanData.AnnotationData a : annotations) {
				if (Objects.equals(a.annotationType(), annotationType)) {
					String memberName = a.memberName();
					pluginClassNames.add(memberName);
				}
			}
		}
		List<T> instances = new ArrayList<>();
		for (String className : pluginClassNames) {
			try {
				Class<?> asmClass = Class.forName(className);
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
				T instance = constructor.newInstance();
				instances.add(instance);
			} catch (ReflectiveOperationException | LinkageError e) {
				LOGGER.error("Failed to load: {}", className, e);
			}
		}
		return instances;
	}
	
	private static final List<IModPlugin> jeiPlugins = getInstances(JeiPlugin.class, IModPlugin.class);
	private static final JemiPlugin jEMI = (JemiPlugin) jeiPlugins.stream().filter(x -> x instanceof JemiPlugin).findFirst().orElseThrow();
	private static final Set<String> modsWithEMIPlugins = JemiUtil.getHandledMods();
	
	static {
		jeiPlugins.removeIf(x -> modsWithEMIPlugins.contains(x.getPluginUid().getNamespace()));
		jeiPlugins.remove(jEMI);
		jeiPlugins.addFirst(jEMI);
	}
	
	private final JEIConfigManager jeiConfigManager = new JEIConfigManager();
	private final JEIRuntime jeiRuntime;
	
	public TooManyRecipeViewersMod() {
		jeiPlugins.forEach(x -> x.onConfigManagerAvailable(jeiConfigManager));
		
		final IRuntimeRegistration runtimeRegistration = new RuntimeRegistration();
		jeiPlugins.forEach(x -> x.registerRuntime(runtimeRegistration));
		
		jeiRuntime = new JEIRuntime(runtimeRegistration, new JEIKeyMappings(), jeiConfigManager);
		jeiPlugins.forEach(x -> x.onRuntimeAvailable(jeiRuntime));
	}
	
}
